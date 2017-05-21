package tw.edu.nkfust.eHat;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

/*
 * Adapted from:
 * http://developer.android.com/samples/BluetoothLeGatt/src/com.example.android.bluetoothlegatt/BluetoothLeService.html
 */
public class RFduinoService extends Service { /**綁定型RFduinoService，與Activity建立聯繫*/
	private final static String TAG = RFduinoService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;			/**藍芽連接連接物件*/
	private BluetoothGattService mBluetoothGattService;

	public final static String ACTION_CONNECTED = "com.rfduino.ACTION_CONNECTED";
	public final static String ACTION_DISCONNECTED = "com.rfduino.ACTION_DISCONNECTED";
	public final static String ACTION_DATA_AVAILABLE = "com.rfduino.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.rfduino.EXTRA_DATA";

	public final static UUID UUID_SERVICE = BluetoothHelper.sixteenBitUuid(0x2220);
	public final static UUID UUID_RECEIVE = BluetoothHelper.sixteenBitUuid(0x2221);
	public final static UUID UUID_SEND = BluetoothHelper.sixteenBitUuid(0x2222);
	public final static UUID UUID_DISCONNECT = BluetoothHelper.sixteenBitUuid(0x2223);
	public final static UUID UUID_CLIENT_CONFIGURATION = BluetoothHelper.sixteenBitUuid(0x2902);

	private int rssi;

	// Implements callback methods for GATT events that the app cares about.
	// For example, connection change and services discovered.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "Connected to RFduino.");
				Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i(TAG, "Disconnected from RFduino.");
				broadcastUpdate(ACTION_DISCONNECTED);
			}// End of if-condition
		}// End of onConnectionStateChange

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				mBluetoothGattService = gatt.getService(UUID_SERVICE);

				if (mBluetoothGattService == null) {
					Log.e(TAG, "RFduino GATT service not found!");
					return;
				}// End of if-condition

				BluetoothGattCharacteristic receiveCharacteristic = mBluetoothGattService.getCharacteristic(UUID_RECEIVE);

				if (receiveCharacteristic != null) {
					BluetoothGattDescriptor receiveConfigDescriptor = receiveCharacteristic.getDescriptor(UUID_CLIENT_CONFIGURATION);

					if (receiveConfigDescriptor != null) {
						gatt.setCharacteristicNotification(receiveCharacteristic, true);
						receiveConfigDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						gatt.writeDescriptor(receiveConfigDescriptor);
					} else {
						Log.e(TAG, "RFduino receive config descriptor not found!");
					}// End of if-condition
				} else {
					Log.e(TAG, "RFduino receive characteristic not found!");
				}// End of if-condition

				broadcastUpdate(ACTION_CONNECTED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}// End of if-condition
		}// End of onServicesDiscovered

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}// End of if-condition
		}// End of onCharacteristicRead

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}// End of onCharacteristicChanged

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				RFduinoService.this.rssi = rssi;
			}// End of if-condition
		}// End of onReadRemoteRssi
	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent, Manifest.permission.BLUETOOTH);
	}// End of broadcastUpdate

	private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
		if (UUID_RECEIVE.equals(characteristic.getUuid())) {
			final Intent intent = new Intent(action);
			intent.putExtra(EXTRA_DATA, characteristic.getValue());
			sendBroadcast(intent, Manifest.permission.BLUETOOTH);
		}// End of if-condition
	}// End of broadcastUpdate

	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder { /**本地綁定者類別*/
		RFduinoService getService() {  /**getService 回傳本地綁定的服務*/
			return RFduinoService.this;  /**該Service為Class自己本身*/
		}// End of getService
	}// End of LocalBinder

	@Override
	public IBinder onBind(Intent intent) { /**回傳綁定者*/
		return mBinder;
	}// End of onBind

	@Override
	public boolean onUnbind(Intent intent) { /**取消綁定*/
		// After using a given device, you should make sure that BluetoothGatt.close() is called such that resources are cleaned up properly.
		// In this particular example, close() is invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}// End of onUnbind


	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 **/
	public boolean initialize() {   /**藍芽服務初始化 */   //需同時取得藍芽系統管理員及檢查開啟藍芽功能，其中一項不符則初始化失敗
		// For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
		if (mBluetoothManager == null) {   /**判斷受否取得藍芽系統管理員*/
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}// End of if-condition
		}// End of if-condition

		mBluetoothAdapter = mBluetoothManager.getAdapter();  /**檢查手機是否開啟藍芽裝置*/

		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}// End of if-condition

		return true;
	}// End of initialize

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address 
	 *        The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully.
	 *		   The connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 **/
	public boolean connect(final String address) { /**藍芽服務連接測試 */
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}// End of if-condition

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
			Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
			return mBluetoothGatt.connect();
		}// End of if-condition

		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address); /** 1.取得藍芽裝置的資訊*/
		// We want to directly connect to the device, so we are setting the autoConnect parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback); /**   2.連接成功取得藍芽連接協定gatt*/
		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address; /**   3.藍芽裝置位址資訊對稱更新*/
		return true;
	}// End of connect

	/**
	 * Disconnects an existing connection or cancel a pending connection.
	 * The disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 **/
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}// End of if-condition

		mBluetoothGatt.disconnect();
	}// End of disconnect

	/**
	 * After using a given BLE device, the app must call this method to ensure resources are released properly.
	 **/
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}// End of if-condition

		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}// End of close

	public void read() {
		if (mBluetoothGatt == null || mBluetoothGattService == null) {
			Log.w(TAG, "BluetoothGatt not initialized");
			return;
		}// End of if-condition

		BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID_RECEIVE);
		mBluetoothGatt.readCharacteristic(characteristic);
	}// End of read

	public boolean send(byte[] data) {
		if (mBluetoothGatt == null || mBluetoothGattService == null) {
			Log.w(TAG, "BluetoothGatt not initialized");
			return false;
		}// End of if-condition

		BluetoothGattCharacteristic characteristic = mBluetoothGattService.getCharacteristic(UUID_SEND);

		if (characteristic == null) {
			Log.w(TAG, "Send characteristic not found");
			return false;
		}// End of if-condition

		characteristic.setValue(data);
		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}// End of send

	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CONNECTED);
		filter.addAction(ACTION_DISCONNECTED);
		filter.addAction(ACTION_DATA_AVAILABLE);
		return filter;
	}// End of getIntentFilter

	public boolean inReadRssi() {
		if (mBluetoothGatt.readRemoteRssi()) {
			return true;
		}// End of if-condition

		return false;
	}// End of readRssi

	public int readRssi() {
		return rssi;
	}// End of reedRssi
}// End of RFduinoService
