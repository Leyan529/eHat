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
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);// ��q�ܪ��ӹq���A�i���ť

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
		mTelephonyManager.listen(new StateListener(), PhoneStateListener.LISTEN_CALL_STATE);// ���U��ť����q�ܪ��A�i���ť
	}// End of register

	public class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			/**
			 * 	�y���I�s�������A (Call)
			 * IDLE: idle ���A�A �L�����������I�s
			 * ACTIVE: �E�����A�A��ܥ��b�q�ܤ�
			 * HOLDING: �I�s�O�����A
			 * DIALING: ���b�o�_�y���I�s���L�{���A�Y�������A�Ȯ��٨S�����q���
			 * ALERTING: �o�_�y���I�s��A���b�_�a���A�A�w���q���A������٨S����ť
			 * INCOMING: �ӹq�_�a���A
			 * WAITING: �I�s���ݪ��A
			 * DISCONNECTED: �q�ܤw���������A�s����������
			 * DISCONNECTING: �q�ܥ��b�_�}���L�{���A�٨S���������_�}
			 * 
			 * 	����������A (Phone)
			 * IDLE: �S���h�q�A�]�S���ӹq�A�e��x Call ���B�� DISCONNECTED �M IDLE ���A
			 * RINGING: ���� Call �� INCOMING �M WAITING ���A
			 * OFFHOOK: �q�ܤ�
			 */

			switch (state) { 
				case TelephonyManager.CALL_STATE_IDLE:// �Ŷ����A
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
				case TelephonyManager.CALL_STATE_RINGING:// �ӹq��
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
				case TelephonyManager.CALL_STATE_OFFHOOK:// �q�ܤ� 
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
