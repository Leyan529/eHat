package tw.edu.nkfust.eHat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback, SensorEventListener,OnMapReadyCallback {
	private LinearLayout tabViewOfDevice, tabViewOfAlarm, tabViewOfCall, tabViewOfMap;// 頁卡標頭
	private ImageView tabImageOfDevice, tabImageOfAlarm, tabImageOfCall, tabImageOfMap;
	private ImageView cursorImage;// 動畫圖片
	private int indexOfCurrent = 0;// 當前頁卡編號
	private int offset = 0;// 動畫圖片偏移量
	private int bmpW;// 動畫圖片寬度

	private ScrollView layoutOfDevice, layoutOfAlarm;
	private LinearLayout layoutOfCall;
	private FrameLayout layoutOfMap;

	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

	// State machine
	final private static int STATE_BLUETOOTH_OFF = 1;
	final private static int STATE_DISCONNECTED = 2;
	final private static int STATE_CONNECTING = 3;
	final private static int STATE_CONNECTED = 4;

	private int state;

	private boolean scanStarted;
	private boolean scanning;
	private boolean connecting;

	protected static BluetoothAdapter mBluetoothAdapter;
	protected static BluetoothA2dp mBluetoothA2dp;
	protected static BluetoothDevice mBluetoothDevice;
	protected static BluetoothDevice mBluetoothHeadset;

	protected static RFduinoService mRFduinoService;
	protected static RFduinoManager mRFduinoManager;

	private Button buttonOfEnableBluetooth, buttonOfScan, buttonOfConnect;
	private TextView textOfScanStatus, textOfDeviceInfo, textOfConnectionStatus;

	private SignalHandler mSignalHandler;
	private int rssi;

	private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);

			if (state == BluetoothAdapter.STATE_ON) {
				upgradeState(STATE_DISCONNECTED);
			} else if (state == BluetoothAdapter.STATE_OFF) {
				downgradeState(STATE_BLUETOOTH_OFF);
			}// End of if-condition
		}// End of onReceive
	};

	private final BroadcastReceiver headsetStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, 0);

			if (state == BluetoothA2dp.STATE_CONNECTED) {
				mBluetoothHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				mBluetoothAdapter.getProfileProxy(MainActivity.this, new ServiceListener() {
					@Override
					public void onServiceConnected(int profile, BluetoothProfile proxy) {
						if (BluetoothProfile.A2DP == profile) {
							mBluetoothA2dp = (BluetoothA2dp) proxy;
						}// End of if-condition
					}// End of on onServiceConnected

					@Override
					public void onServiceDisconnected(int profile) {
						if (BluetoothProfile.A2DP == profile) {
							mBluetoothA2dp = null;
						}// End of if-condition
					}// End of onServiceDisconnected
				}, BluetoothProfile.A2DP);

				Toast.makeText(MainActivity.this, getString(R.string.toast_BluetoothHeadsetOn),Toast.LENGTH_SHORT).show();
			} else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
				mBluetoothA2dp = null;
				Toast.makeText(MainActivity.this, getString(R.string.toast_BluetoothHeadsetOff), Toast.LENGTH_SHORT).show();
			}// End of if-condition
		}// End of onReceive
	};

	private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mRFduinoService = ((RFduinoService.LocalBinder) service).getService();

			if (mRFduinoService.initialize()) {
				if (mRFduinoService.connect(mBluetoothDevice.getAddress())) {
					upgradeState(STATE_CONNECTING);
				}// End of if-condition
			}// End of if-condition
		}// End of onServiceConnected

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mRFduinoService = null;
			downgradeState(STATE_DISCONNECTED);
		}// End of onServiceDisconnected
	};

	private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RFduinoService.ACTION_CONNECTED.equals(action)) {
				upgradeState(STATE_CONNECTED);
			} else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
				Toast.makeText(MainActivity.this, R.string.toast_RFduino_Disconnect, Toast.LENGTH_SHORT).show();
				downgradeState(STATE_DISCONNECTED);
			}// End of if-condition
		}// End of onReceive
	};

	private AlarmHelper mAlarmHelper;
	private CountDownTimer theTimerOne, theTimerTwo, theTimerThree;
	protected static Calendar calendar;
	private int hourOfTimePicker, minuteOfTimePicker;

	private TextView nameOfTheTimerOne, nameOfTheTimerTwo, nameOfTheTimerThree, nameOfTimePicker, textOfTimePickerStatus;
	private Button buttonOfTheTimerOneStatus, buttonOfTheTimerTwoStatus, buttonOfTheTimerThreeStatus;
	private Button buttonOfTimePickerStart, buttonOfTimePickerStop;
	protected static boolean isTimerOneCounting, isTimerTwoCounting, isTimerThreeCounting;

	private CallManager mCallManager;
	protected static CallDatabaseHelper mCallDatabaseHelper;
	private Cursor cursor;
	private SimpleCursorAdapter mSimpleCursorAdapter;

	private EditText editTextOfPersonName, editTextOfPersonPhone;
	private Button buttonOfNewPerson;
	private ListView listViewOfCall;

	private SensorManager mSensorManager;
	private List<Sensor> listSensor;
	private Sensor sensor;
	private float bearing;

	protected static MapFragment mapFragment;
	protected static GoogleMap mMap;
	protected static MapHelper mMapHelper;
	protected static PathHelper mPathHelper;
	private AddressDatabaseHelper mAddressDatabaseHelper;
	protected static LatLng toLatLng, nowLatLng;
	protected static String toAddress;
	protected static MarkerOptions toMarkerOpt, nowMarkerOpt;
	protected static Marker toMarker, nowMarker;

	private Button buttonOfSearchBar, buttonOfFavorite, buttonOfPath, buttonOfSport;
	private LinearLayout layoutOfSearchBar;
	private Button buttonOfSearch;
	private EditText editTextOfSrearch;
	protected static TextView textOfMapDescription;

	protected static boolean guiding;
	protected static String pathMode = "walking";
	protected static String mapMode = "normal";// normal: 導航, sport: 運動
	protected static Timer timerOfGuide, timerOfSport;

	@Override //1.分配資源給這個 Activity(onCreate)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initTabView();
		initCursor();
		initLayout();

		registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		registerReceiver(headsetStateReceiver, new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED));
		registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Enable bluetooth
		buttonOfEnableBluetooth = (Button) findViewById(R.id.buttonOfEnableBluetooth);
		buttonOfEnableBluetooth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonOfEnableBluetooth.setEnabled(false);
				buttonOfEnableBluetooth.setText(mBluetoothAdapter.enable() ? getString(R.string.textOfButton_Enabling_Bluetooth) : getString(R.string.textOfButton_Enable_Failed));
			}// End of onClick
		});

		// Find device
		textOfScanStatus = (TextView) findViewById(R.id.textOfScanStatus);
		buttonOfScan = (Button) findViewById(R.id.buttonOfScan);
		buttonOfScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				scanStarted = !scanStarted;

				if (scanStarted) {
					mBluetoothAdapter.startLeScan(new UUID[] { RFduinoService.UUID_SERVICE }, MainActivity.this);
					updateUi();
				} else {
					mBluetoothAdapter.stopLeScan(MainActivity.this);
					updateUi();
				}// End of if-condition
			}// End of onClick
		});

		// Device info
		textOfDeviceInfo = (TextView) findViewById(R.id.textOfDeviceInfo);

		mRFduinoManager = new RFduinoManager();

		// Connect device
		textOfConnectionStatus = (TextView) findViewById(R.id.textOfConnectionStatus);
		buttonOfConnect = (Button) findViewById(R.id.buttonOfConnect);
		buttonOfConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connecting = !connecting;

				if (connecting) {
					Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
					bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);
					mSignalHandler = new SignalHandler();
					upgradeState(STATE_CONNECTING);
				} else {
					mRFduinoService.disconnect();
					unbindService(rfduinoServiceConnection);
					downgradeState(STATE_DISCONNECTED);
				}// End of if-condition
			}// End of onClick
		});

		mAlarmHelper = new AlarmHelper(MainActivity.this);

		nameOfTheTimerOne = (TextView) findViewById(R.id.textOfTheTimerOne);
		nameOfTheTimerOne.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isTimerOneCounting) {
					LayoutInflater inflater = getLayoutInflater();
					View viewOfEditAlarm = inflater.inflate(R.layout.alarm_dialog, null);
					mAlarmHelper.setAlarm(viewOfEditAlarm, nameOfTheTimerOne, buttonOfTheTimerOneStatus);
				}// End of if-condition
			}// End of onClick
		});

		buttonOfTheTimerOneStatus = (Button) findViewById(R.id.buttonOfTheTimerOneStatus);
		buttonOfTheTimerOneStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isTimerOneCounting) {
					theTimerOne = mAlarmHelper.getTimer(buttonOfTheTimerOneStatus);
					theTimerOne.start();
				} else {
					theTimerOne.cancel();
					isTimerOneCounting = false;
					buttonOfTheTimerOneStatus.setText(R.string.textOfInitTimer);
					buttonOfTheTimerOneStatus.setEnabled(false);
				}// End of if-condition
			}// End of onClick
		});

		nameOfTheTimerTwo = (TextView) findViewById(R.id.textOfTheTimerTwo);
		nameOfTheTimerTwo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isTimerTwoCounting) {
					LayoutInflater inflater = getLayoutInflater();
					View viewOfEditAlarm = inflater.inflate(R.layout.alarm_dialog, null);
					mAlarmHelper.setAlarm(viewOfEditAlarm, nameOfTheTimerTwo, buttonOfTheTimerTwoStatus);
				}// End of if-condition
			}// End of onClick
		});

		buttonOfTheTimerTwoStatus = (Button) findViewById(R.id.buttonOfTheTimerTwoStatus);
		buttonOfTheTimerTwoStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isTimerTwoCounting) {
					theTimerTwo = mAlarmHelper.getTimer(buttonOfTheTimerTwoStatus);
					theTimerTwo.start();
				} else {
					theTimerTwo.cancel();
					isTimerTwoCounting = false;
					buttonOfTheTimerTwoStatus.setText(R.string.textOfInitTimer);
					buttonOfTheTimerTwoStatus.setEnabled(false);
				}// End of if-condition
			}// End of onClick
		});

		nameOfTheTimerThree = (TextView) findViewById(R.id.textOfTheTimerThree);
		nameOfTheTimerThree.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isTimerThreeCounting) {
					LayoutInflater inflater = getLayoutInflater();
					View viewOfEditAlarm = inflater.inflate(R.layout.alarm_dialog, null);
					mAlarmHelper.setAlarm(viewOfEditAlarm, nameOfTheTimerThree, buttonOfTheTimerThreeStatus);
				}// End of if-condition
			}// End of onClick
		});

		buttonOfTheTimerThreeStatus = (Button) findViewById(R.id.buttonOfTheTimerThreeStatus);
		buttonOfTheTimerThreeStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isTimerThreeCounting) {
					theTimerThree = mAlarmHelper.getTimer(buttonOfTheTimerThreeStatus);
					theTimerThree.start();
				} else {
					theTimerThree.cancel();
					isTimerThreeCounting = false;
					buttonOfTheTimerThreeStatus.setText(R.string.textOfInitTimer);
					buttonOfTheTimerThreeStatus.setEnabled(false);
				}// End of if-condition
			}// End of onClick
		});

		calendar = Calendar.getInstance();
		hourOfTimePicker = calendar.get(Calendar.HOUR_OF_DAY);
		minuteOfTimePicker = calendar.get(Calendar.MINUTE);

		nameOfTimePicker = (TextView) findViewById(R.id.textOfTimePicker);
		nameOfTimePicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
							if (hourOfDay >= 12) {
								textOfTimePickerStatus.setText(String.format("下午	%02d : %02d", hourOfDay - 12, minute));
							} else {
								textOfTimePickerStatus.setText(String.format("上午	%02d : %02d", hourOfDay, minute));
							}// End of if-condition

							mAlarmHelper.setTimePicker(hourOfDay, minute);
							buttonOfTimePickerStart.setEnabled(true);
						}// End of onTimeSet
				}, hourOfTimePicker, minuteOfTimePicker, true).show();
			}// End of onClick
		});
		textOfTimePickerStatus = (TextView) findViewById(R.id.textOfTimePickerStatus);

		buttonOfTimePickerStart = (Button) findViewById(R.id.buttonOfTimePickerStart);
		buttonOfTimePickerStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mAlarmHelper.startTimePicker();
				buttonOfTimePickerStart.setEnabled(false);
				buttonOfTimePickerStop.setEnabled(true);
			}// End of onClick
		});

		buttonOfTimePickerStop = (Button) findViewById(R.id.buttonOfTimePickerStop);
		buttonOfTimePickerStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlarmHelper.stopTimePicker();
				buttonOfTimePickerStart.setEnabled(true);
				buttonOfTimePickerStop.setEnabled(false);
			}// End of onClick
		});

		mCallManager = new CallManager(MainActivity.this);
		mCallManager.register();

		// Create SQLiteOpenHelper
		mCallDatabaseHelper = new CallDatabaseHelper(MainActivity.this);
		cursor = mCallDatabaseHelper.get();
		mSimpleCursorAdapter = new SimpleCursorAdapter(MainActivity.this,
				R.layout.call_adapter, cursor,
				new String[] { "Name", "Phone" },
				new int[] { R.id.textOfTitle, R.id.textOfSubtitle });

		editTextOfPersonName = (EditText) findViewById(R.id.editTextOfPersonName);
		editTextOfPersonPhone = (EditText) findViewById(R.id.editTextOfPersonPhone);

		buttonOfNewPerson = (Button) findViewById(R.id.buttonOfNewPerson);
		buttonOfNewPerson.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editTextOfPersonName.getText().toString().equals("")) {
					Toast.makeText(MainActivity.this, R.string.toast_ErOfWriteName, Toast.LENGTH_SHORT).show();
				} else if (editTextOfPersonPhone.getText().toString().equals("")) {
					Toast.makeText(MainActivity.this, R.string.toast_ErOfWritePhone, Toast.LENGTH_SHORT).show();
				} else {
					mCallDatabaseHelper.insert(editTextOfPersonName.getText().toString(), editTextOfPersonPhone.getText().toString());
					cursor.requery();
					listViewOfCall.setAdapter(mSimpleCursorAdapter);
					editTextOfPersonName.setText("");
					editTextOfPersonPhone.setText("");
					Toast.makeText(MainActivity.this, R.string.toast_AddTheNewPerson, Toast.LENGTH_SHORT).show();
				}// End of if-condition
			}// End of onClick
		});

		listViewOfCall = (ListView) findViewById(R.id.listViewOfCall);
		listViewOfCall.setAdapter(mSimpleCursorAdapter);
		listViewOfCall.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final int pos = position;
				final String name = cursor.getString(1).toString();
				final String phone = cursor.getString(2).toString();

				new AlertDialog.Builder(MainActivity.this)
						.setItems(new String[] { getString(R.string.textOfMenuItem_Dial), getString(R.string.textOfMenuItem_Edit), getString(R.string.textOfMenuItem_Delete) },
								new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											switch (which) {
												case 0:
													startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
													break;
												case 1:
													LayoutInflater inflater = getLayoutInflater();
													View viewOfEditCall = inflater.inflate(R.layout.call_dialog, null);
													final EditText editTextOfPersonName = (EditText) viewOfEditCall.findViewById(R.id.editTextOfPersonName);
													final EditText editTextOfPhone = (EditText) viewOfEditCall.findViewById(R.id.editTextOfPersonPhone);
													editTextOfPersonName.setText(name);
													editTextOfPhone.setText(phone);

													new AlertDialog.Builder(MainActivity.this)
															.setTitle(R.string.title_EditOfPersonDialog)
															.setView(viewOfEditCall)
															.setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
																@Override
																public void onClick(DialogInterface dialog, int which) {
																	if ((editTextOfPersonName.getText().toString().equals(""))) {
																		Toast.makeText(MainActivity.this, R.string.toast_ErOfWriteName, Toast.LENGTH_SHORT).show();
																	} else if (editTextOfPhone.getText().toString().equals("")) {
																		Toast.makeText(MainActivity.this, R.string.toast_ErOfWritePhone, Toast.LENGTH_SHORT).show();
																	} else {
																		cursor.moveToPosition(pos);
																		mCallDatabaseHelper.edit(cursor.getInt(0), editTextOfPersonName.getText().toString(), editTextOfPhone.getText().toString());
																		cursor.requery();
																		listViewOfCall.setAdapter(mSimpleCursorAdapter);
																	}// End of if-condition
																}// End of onClick
															})
															.setNegativeButton(R.string.textOfButton_DialogNo, null)
															.show();
													break;
												case 2:
													new AlertDialog.Builder(MainActivity.this)
															.setTitle(R.string.title_DeleteOfPersonDialog)
															.setMessage(R.string.message_CheckToDeleteThePerson)
															.setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
																@Override
																public void onClick(DialogInterface dialog, int which) {
																	cursor.moveToPosition(pos);
																	mCallDatabaseHelper.delete(cursor.getInt(0));
																	cursor.requery();
																	listViewOfCall.setAdapter(mSimpleCursorAdapter);
																	Toast.makeText(MainActivity.this, R.string.toast_DeleteThePerson, Toast.LENGTH_SHORT).show();
																}// End of onClick
															})
															.setNegativeButton(R.string.textOfButton_DialogNo, null)
															.show();
													break;
											}// End of switch-condition
										}// End of onClick
								})
								.show();

						return false;
					}// End of onItemLongClick
				});

		mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mMapHelper = new MapHelper(MainActivity.this);
		mPathHelper = new PathHelper(MainActivity.this);
	}// End of onCreate

	@Override //2.將 Activity 內容顯示到螢幕上(onStart)
	protected void onStart() {
		super.onStart();
		updateState(mBluetoothAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
		wakeLock.acquire();
	}// End of onStart

	@Override //3.在一切就緒後, 取得螢幕的控制權(onResume), 使用者可以開始使用這個程式，並把保存的資料拿回來使用.
	protected void onResume(){
		super.onResume();
	}

	@Override //4.凍結原本的 Activity, 再交出直接存取螢幕能力(onPause )，並把需要保存的資料保存
	protected void onPause(){
		super.onPause();
	}

	@Override //5.代表該Activity為停止，仍保留使用者原先輸入之內容，但使用者已經完全看不到這個畫面。除非手機上的記憶體嚴重不足，則有可能遭到關閉
	protected void onStop() {
		super.onStop();
		wakeLock.release();
	}// End of onStop

	@Override //6.將停止運作的Activity重新啟動，常見情況為使用者按下Back鍵回到原本的 Activity
	protected void onRestart() {
		super.onRestart();
	}

	@Override //7.銷毀(Destroy)停止運作的Activity上的所有資源並釋放
	protected void onDestroy() {
		super.onDestroy();
		mBluetoothAdapter.stopLeScan(this);
		unregisterReceiver(bluetoothStateReceiver);
		unregisterReceiver(headsetStateReceiver);
		unregisterReceiver(rfduinoReceiver);

		if (mRFduinoService != null) unbindService(rfduinoServiceConnection);

		mCallDatabaseHelper.close();
		mAddressDatabaseHelper.close();
	}// End of onDestroy

	/**
	 * 初始化頁卡頭標
	 */
	private void initTabView() {
		tabViewOfDevice = (LinearLayout) findViewById(R.id.tabViewOfDevice);
		tabViewOfAlarm = (LinearLayout) findViewById(R.id.tabViewOfAlarm);
		tabViewOfCall = (LinearLayout) findViewById(R.id.tabViewOfCall);
		tabViewOfMap = (LinearLayout) findViewById(R.id.tabViewOfMap);
		tabViewOfDevice.setOnClickListener(new TabViewOnClickListener(0));
		tabViewOfAlarm.setOnClickListener(new TabViewOnClickListener(1));
		tabViewOfCall.setOnClickListener(new TabViewOnClickListener(2));
		tabViewOfMap.setOnClickListener(new TabViewOnClickListener(3));
	}// End of InitTabView

	@Override
	public void onMapReady(GoogleMap googleMap) {
		{
			mMap = googleMap;
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			mMap.setMyLocationEnabled(true);
			mMap.setTrafficEnabled(true);
			mMap.setIndoorEnabled(true);
			mMap.setBuildingsEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(true);

			mMap.setBuildingsEnabled(true);// Turns the 3D buildings layer on or off
			mMap.setIndoorEnabled(true);// Sets whether indoor maps should be enabled
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);// Sets the type of map tiles that should be displayed
			mMap.setMyLocationEnabled(true);// Enables or disables the my-location layer
			mMap.setTrafficEnabled(false);// Turns the traffic layer on or off
			mMap.getUiSettings().setMapToolbarEnabled(false);
			setMapUi();
		}
	}

	/**
	 * 監聽頁卡標頭點擊
	 */
	private class TabViewOnClickListener implements OnClickListener {
		private int index = 0;

		public TabViewOnClickListener(int i) {
			index = i;
		}// End of structure

		@Override
		public void onClick(View arg0) {
			int one = offset * 2 + bmpW;// 頁卡1 -> 頁卡2 的偏移量
			int two = one * 2;// 頁卡1 -> 頁卡3 的偏移量
			int three = one * 3;// 頁卡1 -> 頁卡4 的偏移量

			Animation animation = null;
			tabImageOfDevice = (ImageView) findViewById(R.id.tabImageOfDevice);
			tabImageOfAlarm = (ImageView) findViewById(R.id.tabImageOfAlarm);
			tabImageOfCall = (ImageView) findViewById(R.id.tabImageOfCall);
			tabImageOfMap = (ImageView) findViewById(R.id.tabImageOfMap);

			if (indexOfCurrent != index) {
				switch (index) {
					case 0:
						if (indexOfCurrent == 1) {
							animation = new TranslateAnimation(one, 0, 0, 0);
							tabImageOfAlarm.setImageResource(R.drawable.ic_alarm_grey600_36dp);
							layoutOfAlarm.setVisibility(View.GONE);
						} else if (indexOfCurrent == 2) {
							animation = new TranslateAnimation(two, 0, 0, 0);
							tabImageOfCall.setImageResource(R.drawable.ic_call_grey600_36dp);
							layoutOfCall.setVisibility(View.GONE);
						} else if (indexOfCurrent == 3) {
							animation = new TranslateAnimation(three, 0, 0, 0);
							tabImageOfMap.setImageResource(R.drawable.ic_explore_grey600_36dp);
							layoutOfMap.setVisibility(View.GONE);
						}// End of if-condition

						tabImageOfDevice.setImageResource(R.drawable.ic_phone_android_black_36dp);
						layoutOfDevice.setVisibility(View.VISIBLE);
						break;
					case 1:
						if (indexOfCurrent == 0) {
							animation = new TranslateAnimation(offset, one, 0, 0);
							tabImageOfDevice.setImageResource(R.drawable.ic_phone_android_grey600_36dp);
							layoutOfDevice.setVisibility(View.GONE);
						} else if (indexOfCurrent == 2) {
							animation = new TranslateAnimation(two, one, 0, 0);
							tabImageOfCall.setImageResource(R.drawable.ic_call_grey600_36dp);
							layoutOfCall.setVisibility(View.GONE);
						} else if (indexOfCurrent == 3) {
							animation = new TranslateAnimation(three, one, 0, 0);
							tabImageOfMap.setImageResource(R.drawable.ic_explore_grey600_36dp);
							layoutOfMap.setVisibility(View.GONE);
						}// End of if-condition

						tabImageOfAlarm.setImageResource(R.drawable.ic_alarm_black_36dp);
						layoutOfAlarm.setVisibility(View.VISIBLE);
						break;
					case 2:
						if (indexOfCurrent == 0) {
							animation = new TranslateAnimation(offset, two, 0, 0);
							tabImageOfDevice.setImageResource(R.drawable.ic_phone_android_grey600_36dp);
							layoutOfDevice.setVisibility(View.GONE);
						} else if (indexOfCurrent == 1) {
							animation = new TranslateAnimation(one, two, 0, 0);
							tabImageOfAlarm.setImageResource(R.drawable.ic_alarm_grey600_36dp);
							layoutOfAlarm.setVisibility(View.GONE);
						} else if (indexOfCurrent == 3) {
							animation = new TranslateAnimation(three, two, 0, 0);
							tabImageOfMap.setImageResource(R.drawable.ic_explore_grey600_36dp);
							layoutOfMap.setVisibility(View.GONE);
						}// End of if-condition

						tabImageOfCall.setImageResource(R.drawable.ic_call_black_36dp);
						layoutOfCall.setVisibility(View.VISIBLE);
						break;
					case 3:
						if (indexOfCurrent == 0) {
							animation = new TranslateAnimation(offset, three, 0, 0);
							tabImageOfDevice.setImageResource(R.drawable.ic_phone_android_grey600_36dp);
							layoutOfDevice.setVisibility(View.GONE);
						} else if (indexOfCurrent == 1) {
							animation = new TranslateAnimation(one, three, 0, 0);
							tabImageOfAlarm.setImageResource(R.drawable.ic_alarm_grey600_36dp);
							layoutOfAlarm.setVisibility(View.GONE);
						} else if (indexOfCurrent == 2) {
							animation = new TranslateAnimation(two, three, 0, 0);
							tabImageOfCall.setImageResource(R.drawable.ic_call_grey600_36dp);
							layoutOfCall.setVisibility(View.GONE);
						}// End of if-condition

						tabImageOfMap.setImageResource(R.drawable.ic_explore_black_36dp);
						layoutOfMap.setVisibility(View.VISIBLE);

						if (mMapHelper.checkService()) {
							buttonOfSearchBar.setVisibility(View.VISIBLE);
							buttonOfFavorite.setVisibility(View.VISIBLE);
							buttonOfPath.setVisibility(View.VISIBLE);
							buttonOfSport.setVisibility(View.VISIBLE);
						} else {
							buttonOfSearchBar.setVisibility(View.GONE);
							buttonOfFavorite.setVisibility(View.GONE);
							buttonOfPath.setVisibility(View.GONE);
							buttonOfSport.setVisibility(View.GONE);
						}// End of if-condition

						break;
				}// End of switch-condition

				indexOfCurrent = index;
				animation.setFillAfter(true);// True:圖片停在動畫結束的位置
				animation.setDuration(300);
				cursorImage.startAnimation(animation);
			}// End of if-condition
		}// End of onClick
	}// End of TabViewOnClickListener

	/**
	 * 初始化動畫圖片
	 */
	private void initCursor() {
		cursorImage = (ImageView) findViewById(R.id.cursorImage);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.cursor_blue_60x6).getWidth();// 獲取圖片寬度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 獲取分辨率寬度
		offset = (screenW / 4 - bmpW) / 2;// 計算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursorImage.setImageMatrix(matrix);// 設置動畫初始位置
	}// End of InitCursor

	/**
	 * 初始化頁面
	 */
	private void initLayout() {
		layoutOfDevice = (ScrollView) findViewById(R.id.layoutOfDevice);
		layoutOfAlarm = (ScrollView) findViewById(R.id.layoutOfAlarm);
		layoutOfCall = (LinearLayout) findViewById(R.id.layoutOfCall);
		layoutOfMap = (FrameLayout) findViewById(R.id.layoutOfMap);
	}// End of InitLayout







	private void upgradeState(int newState) {
		if (newState > state) {
			updateState(newState);
		}// End of if-condition
	}// End of upgradeState

	private void downgradeState(int newState) {
		if (newState < state) {
			updateState(newState);
		}// End of if-condition
	}// End of downgradeState

	private void updateState(int newState) {
		state = newState;
		updateUi();
	}// End of updateState

	private void updateUi() {
		// Enable bluetooth
		boolean on = state > STATE_BLUETOOTH_OFF;
		buttonOfEnableBluetooth.setEnabled(!on);
		buttonOfEnableBluetooth.setText(on ? getString(R.string.textOfButton_Enabled_Bluetooth) : getString(R.string.textOfButton_Enable_Bluetooth));

		// Scan
		buttonOfScan.setEnabled(on);

		if (scanStarted) {
			scanning = (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);

			if (scanning) {
				textOfScanStatus.setText(R.string.textOfScanInfo_Scanning);
				buttonOfScan.setText(R.string.textOfButton_Stop);
			}// End of if-condition
		} else {
			textOfScanStatus.setText("");
			buttonOfScan.setText(R.string.textOfButton_Scan);
		}// End of if-condition

		// Connect
		String connectionText = getString(R.string.textOfConnectInfo_DisConnected);

		if (state == STATE_CONNECTING) {
			buttonOfScan.setEnabled(false);
			connectionText = getString(R.string.textOfConnectInfo_Connecting);
			buttonOfConnect.setText(R.string.textOfButton_Separate);
		} else if (state == STATE_CONNECTED) {
			buttonOfScan.setEnabled(false);
			connectionText = getString(R.string.textOfConnectInfo_Connected);
			buttonOfConnect.setText(R.string.textOfButton_Separate);
			mSignalHandler.start();
		}// End of if-condition

		textOfConnectionStatus.setText(connectionText);

		if (textOfConnectionStatus.getText().toString().equals(getString(R.string.textOfConnectInfo_DisConnected))) {
			connecting = false;
			buttonOfConnect.setText(R.string.textOfButton_Connect);
		}// End of if-condition

		buttonOfConnect.setEnabled(mBluetoothDevice != null && on);
	}// End of updateUi

	@Override
	public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
		mBluetoothAdapter.stopLeScan(this);
		scanStarted = false;
		mBluetoothDevice = device;

		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textOfDeviceInfo.setText(BluetoothHelper.getDeviceInfoText(mBluetoothDevice, rssi, scanRecord));
				updateUi();
			}// End of run
		});
	}// End of onLeScan

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			bearing = event.values[SensorManager.DATA_X];
		}// End of if-condition
	}// End of onSensorChanged

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}// End of onAccuracyChanged

	class SignalHandler extends Thread {
		@Override
		public void run() {
			try {
				while (connecting) {
					if (mRFduinoService.inReadRssi()) {
						Thread.sleep(4000);
						rssi = mRFduinoService.readRssi();

						if (rssi < -70) {
							mRFduinoManager.onStateChanged(0);
						} else if (mRFduinoManager.isOutOfRange()) {
							mRFduinoManager.onStateChanged(1);
						}// End of if-condition
					}// End of if-condition
				}// End of while-loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}// End of try-catch
		}// End of run
	}// End of SignalHandler

	public void setMapUi(){
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng arg0) {
				if (layoutOfSearchBar.getVisibility() == View.VISIBLE) layoutOfSearchBar.setVisibility(View.GONE);

				if (toMarker != null) toMarker.remove();
			}// End of onMapClick
		});

		mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				if (toMarker != null) toMarker.remove();

				toLatLng = latLng;
				toAddress = mMapHelper.latLngToAddress(toLatLng.latitude, toLatLng.longitude);
				toMarkerOpt = mMapHelper.setMarker(toLatLng.latitude, toLatLng.longitude);
				toMarker = mMap.addMarker(toMarkerOpt);
			}// End of onMapLongClick
		});

		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(final Marker marker) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(toAddress)
						.setItems(new String[] { getString(R.string.textOfMenuItem_Draw), getString(R.string.textOfMenuItem_NewFavorite), getString(R.string.textOfMenuItem_Delete) }, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0:
										nowLatLng = mMapHelper.presentLatLng();
										mPathHelper.getPath(nowLatLng, toLatLng);
										break;
									case 1:
										LayoutInflater inflater = getLayoutInflater();
										View viewOfNewAddress = inflater.inflate(R.layout.address_dialog, null);
										final EditText editTextOfAddressName = (EditText) viewOfNewAddress.findViewById(R.id.editTextOfAddressName);

										new AlertDialog.Builder(MainActivity.this)
												.setTitle(R.string.title_NewAddress)
												.setView(viewOfNewAddress)
												.setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														if ((editTextOfAddressName.getText().toString().equals(""))) {
															Toast.makeText(MainActivity.this, R.string.toast_ErOfWriteAddressName, Toast.LENGTH_SHORT).show();
														} else {
															mAddressDatabaseHelper.insert(editTextOfAddressName.getText().toString(), mMapHelper.latLngToAddress(marker.getPosition().latitude, marker.getPosition().longitude));
															Toast.makeText(MainActivity.this, R.string.toast_AddTheNewAddress, Toast.LENGTH_SHORT).show();
														}// End of if-condition
													}// End of onClick
												})
												.setNegativeButton(R.string.textOfButton_DialogNo, null).show();
										break;
									case 2:
										marker.remove();
										break;
								}// End of switch-condition
							}// End of onClick
						})
						.show();

				return false;
			}// End of onMarkerClick
		});

		layoutOfSearchBar = (LinearLayout) findViewById(R.id.layoutOfSearchBar);

		final InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		editTextOfSrearch = (EditText) findViewById(R.id.editTextOfSearch);
		buttonOfSearch = (Button) findViewById(R.id.buttonOfSearch);
		buttonOfSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!editTextOfSrearch.getText().toString().equals("")) {
					toAddress = editTextOfSrearch.getText().toString();

					if (mMapHelper.addressToLatLng(toAddress) != null) {
						keyboard.toggleSoftInput(0 , InputMethodManager.HIDE_NOT_ALWAYS);

						if (toMarker != null) toMarker.remove();

						toLatLng = mMapHelper.addressToLatLng(toAddress);
						toMarkerOpt = mMapHelper.setMarker(toLatLng.latitude, toLatLng.longitude);
						toMarker = mMap.addMarker(toMarkerOpt);
						mMap.animateCamera(CameraUpdateFactory.newLatLng(toLatLng));
					} else {
						Toast.makeText(MainActivity.this, R.string.toast_ErOfToAddress, Toast.LENGTH_SHORT).show();
					}// End of if-condition
				} else {
					Toast.makeText(MainActivity.this, R.string.toast_ErOfToAddress, Toast.LENGTH_SHORT).show();
				}// End of if-condition
			}// End of onClick
		});

		buttonOfSearchBar = (Button) findViewById(R.id.buttonOfSearchBar);
		buttonOfSearchBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutOfSearchBar.getVisibility() == View.VISIBLE) {
					layoutOfSearchBar.setVisibility(View.GONE);
				} else if (layoutOfSearchBar.getVisibility() == View.GONE) {
					layoutOfSearchBar.setVisibility(View.VISIBLE);
				}// End of if-condition
			}// End of onClick
		});

		mAddressDatabaseHelper = new AddressDatabaseHelper(MainActivity.this);

		buttonOfFavorite = (Button) findViewById(R.id.buttonOfFavorite);
		buttonOfFavorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, AddressListActivity.class));
			}// End of onClick
		});

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		listSensor = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

		if (listSensor.size() > 0) {
			sensor = listSensor.get(0);
			Boolean mRegisteredSensor = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
		}// End of if-condition

		buttonOfPath = (Button) findViewById(R.id.buttonOfPath);
		buttonOfPath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (guiding) {
					startActivity(new Intent(MainActivity.this, PathListActivity.class));
				} else {
					if (toMarker != null) {
						guiding = true;
						nowLatLng = mMapHelper.presentLatLng();
						mPathHelper.getPath(nowLatLng, toLatLng);

						mMapHelper.setRatio(20);
						mMapHelper.setBearing(bearing);
						mMapHelper.setTilt(45);
						mMapHelper.updateMap(nowLatLng.latitude, nowLatLng.longitude);

						timerOfGuide = new Timer();
						timerOfGuide.scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (JSONParser.jInstructions[0] != null) {
											mMap.clear();
											toMarker = mMap.addMarker(toMarkerOpt);
											nowLatLng = mMapHelper.presentLatLng();
											mMapHelper.updateMap(nowLatLng.latitude, nowLatLng.longitude);
											textOfMapDescription.setText(JSONParser.jInstructions[0]);
											mPathHelper.getPath(nowLatLng, toLatLng);

											LatLng nextLatLng = new LatLng(((LatLng) JSONParser.jPoints.get(0)).latitude, ((LatLng) JSONParser.jPoints.get(0)).longitude);
											float[] results = new float[3]; //計算結果沒有放入results陣列
											float len = 0;

											Location.distanceBetween(nowLatLng.latitude, nowLatLng.longitude, nextLatLng.latitude, nextLatLng.longitude, results);
											len = results[0];

											if (len < 100) {
												if (!mRFduinoManager.isOutOfRange()) {
													if (JSONParser.jManeuvers[0].equals("turn-right")) {
														mRFduinoManager.onStateChanged(13);
													} else if (JSONParser.jManeuvers[0].equals("turn-left")) {
														mRFduinoManager.onStateChanged(12);
													} else if (JSONParser.jManeuvers[0].equals("turn-slight-right")) {
														mRFduinoManager.onStateChanged(16);
													} else if (JSONParser.jManeuvers[0].equals("turn-slight-left")) {
														mRFduinoManager.onStateChanged(15);
													}// End of if-condition
												}// End of if-condition
											} else {
												if (!mRFduinoManager.isOutOfRange()) mRFduinoManager.onStateChanged(14);
											}// End of if-condition
										}// End of if-condition
									}// End of run
								});
							}// End of run
						}, 5000, 3000);
						Toast.makeText(MainActivity.this, R.string.toast_NavigationModeOpen, Toast.LENGTH_SHORT).show();
					}// End of if-condition
				}// End of if-condition
			}// End of onClick
		});

		textOfMapDescription = (TextView) findViewById(R.id.textOfMapDescription);

		buttonOfSport = (Button) findViewById(R.id.buttonOfSport);
		buttonOfSport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mapMode.equals("normal")) {
					LayoutInflater inflater = getLayoutInflater();
					View viewOfSport = inflater.inflate(R.layout.sport_dialog, null);
					final EditText editTextOfGoalLength = (EditText) viewOfSport.findViewById(R.id.editTextOfGoalLength);

					new AlertDialog.Builder(MainActivity.this)
							.setView(viewOfSport)
							.setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if ((!editTextOfGoalLength.getText().toString().equals(""))) {
										mapMode = "sport";
										mMap.clear();
										nowLatLng = mMapHelper.presentLatLng();
										nowMarkerOpt = mMapHelper.setMarker(nowLatLng.latitude, nowLatLng.longitude);
										nowMarkerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
										nowMarker = mMap.addMarker(nowMarkerOpt);

										mMapHelper.setRatio(20);
										mMapHelper.setBearing(0);
										mMapHelper.setTilt(30);
										mMapHelper.updateMap(nowLatLng.latitude, nowLatLng.longitude);

										mMapHelper.startSport(nowLatLng.latitude, nowLatLng.longitude, Integer.valueOf(editTextOfGoalLength.getText().toString()));

										timerOfSport = new Timer();
										timerOfSport.scheduleAtFixedRate(new TimerTask() {
											@Override
											public void run() {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														nowLatLng = mMapHelper.presentLatLng();

														if (mMapHelper.recordPath(nowLatLng.latitude, nowLatLng.longitude)) {
															nowMarker = mMap.addMarker(nowMarkerOpt);
															mMap.animateCamera(CameraUpdateFactory.newLatLng(nowLatLng));
														}// End of if-condition
													}// End of run
												});
											}// End of run
										}, 2000, 3000);
									}// End of if-condition
								}// End of onClick
							})
							.setNegativeButton(R.string.textOfButton_DialogNo, null).show();
				} else if (mapMode.equals("sport")) {
					mapMode = "normal";
					mMap.clear();
					timerOfSport.cancel();
					textOfMapDescription.setText("");
					nowLatLng = mMapHelper.presentLatLng();

					mMapHelper.setRatio(15);
					mMapHelper.setBearing(0);
					mMapHelper.setTilt(30);
					mMapHelper.updateMap(nowLatLng.latitude, nowLatLng.longitude);
					Toast.makeText(MainActivity.this, R.string.toast_SportModeClose, Toast.LENGTH_SHORT).show();
				}// End of if-condition
			}// End of if-condition
		});
	}
}// End of MainActivity
