package tw.edu.nkfust.eHat;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class CallManager {
	private Context context;
	private TelephonyManager mTelephonyManager;
	private ITelephony iTelephony;

	private int oldState;

	public CallManager(Context context) {
		this.context = context;
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);// 對電話的來電狀態進行監聽

		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;

		try {
			getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
			getITelephonyMethod.setAccessible(true);
			iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}// End of try-catch
	}// End of structure

	public void register() {
		mTelephonyManager.listen(new StateListener(), PhoneStateListener.LISTEN_CALL_STATE);// 註冊監聽器對電話狀態進行監聽
	}// End of register

	public class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			/**
			 * 	語音呼叫相關狀態 (Call)
			 * IDLE: idle 狀態， 無任何類型的呼叫
			 * ACTIVE: 激活狀態，表示正在通話中
			 * HOLDING: 呼叫保持狀態
			 * DIALING: 正在發起語音呼叫的過程中，即撥號中，暫時還沒有接通對方
			 * ALERTING: 發起語音呼叫後，正在震鈴狀態，已接通對方，但對方還沒有接聽
			 * INCOMING: 來電震鈴狀態
			 * WAITING: 呼叫等待狀態
			 * DISCONNECTED: 通話已完全結束，連接完全釋放
			 * DISCONNECTING: 通話正在斷開的過程中，還沒有完全的斷開
			 * 
			 * 	手機相關狀態 (Phone)
			 * IDLE: 沒有去電，也沒有來電，前後台 Call 都處於 DISCONNECTED 和 IDLE 狀態
			 * RINGING: 對應 Call 的 INCOMING 和 WAITING 狀態
			 * OFFHOOK: 電話中
			 */

			switch (state) { 
				case TelephonyManager.CALL_STATE_IDLE:// 空閒狀態
					if (oldState != state) {
						if (MainActivity.mBluetoothA2dp != null) {
							try {
								String name = "disconnect";
								MainActivity.mBluetoothA2dp.getClass().getMethod(name, BluetoothDevice.class).invoke(MainActivity.mBluetoothA2dp, MainActivity.mBluetoothHeadset);
							} catch (Exception e) {
								e.printStackTrace();
							}// End of try-catch
						}// End of if-condition

						if (!MainActivity.mRFduinoManager.isOutOfRange()) MainActivity.mRFduinoManager.onStateChanged(11);
					}// End of if-condition

					break;
				case TelephonyManager.CALL_STATE_RINGING:// 來電時
					if (oldState == TelephonyManager.CALL_STATE_IDLE) {
						Cursor cursor = MainActivity.mCallDatabaseHelper.query(incomingNumber);
						cursor.moveToFirst();

						if (cursor.getCount() > 0) {
							Toast.makeText(context, cursor.getString(0) + context.getString(R.string.toast_Callin), Toast.LENGTH_LONG).show();

							if (MainActivity.mBluetoothHeadset != null) {
								try {
									String name = "connect";
									MainActivity.mBluetoothA2dp.getClass().getMethod(name, BluetoothDevice.class).invoke(MainActivity.mBluetoothA2dp, MainActivity.mBluetoothHeadset);

									AnswerCall answer = new AnswerCall();
									answer.start();
								} catch (Exception e) {
									e.printStackTrace();
								}// End of try-catch
							}// End of if-condition
						} else {
							if (MainActivity.mBluetoothA2dp != null) {
								try {
									String name = "disconnect";
									MainActivity.mBluetoothA2dp.getClass().getMethod(name, BluetoothDevice.class).invoke(MainActivity.mBluetoothA2dp, MainActivity.mBluetoothHeadset);
								} catch (Exception e) {
									e.printStackTrace();
								}// End of try-catch
							}// End of if-condition
						}// End of if-condition

						if (!MainActivity.mRFduinoManager.isOutOfRange()) MainActivity.mRFduinoManager.onStateChanged(10);
					}// End of if-condition

					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:// 電話中 
					if (!MainActivity.mRFduinoManager.isOutOfRange()) MainActivity.mRFduinoManager.onStateChanged(11);
					break;
			}// End of switch-condition

			oldState = state;
		}// End of onCallStateChanged
	}// End of StateListener

	class AnswerCall extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
				iTelephony.answerRingingCall();
			} catch (Exception e) {
				Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
				KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
				intent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
				context.sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
				intent = new Intent("android.intent.action.MEDIA_BUTTON");
				keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
				intent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
				context.sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
			}// End of try-catch
		}// End of run
	}// End of AnswerCall
}// End of CallManager
