package tw.edu.nkfust.eHat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class PathListActivity extends AppCompatActivity {
	private ListView listViewOfPath;
	private RadioButton buttonOfDrive, buttonOfWalk;
	private RadioGroup groupOfPathMode;
	private Button buttonOfRemove, buttonOfExit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_path_list);

		buttonOfDrive = (RadioButton) findViewById(R.id.buttonOfDriveMode);
		buttonOfWalk = (RadioButton) findViewById(R.id.buttonOfWalkMode);

		if (MainActivity.pathMode.equals("driving")) {
			buttonOfDrive.setChecked(true);
		} else { 
			buttonOfWalk.setChecked(true);
		}// End of if-condition

		groupOfPathMode = (RadioGroup) findViewById(R.id.groupOfPathMode);
		groupOfPathMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.buttonOfDriveMode:
						MainActivity.pathMode = "driving";
						listViewOfPath.setAdapter(new ArrayAdapter<String>(PathListActivity.this, android.R.layout.simple_list_item_1, JSONParser.jInstructions));
						break;
					case R.id.buttonOfWalkMode:
						MainActivity.pathMode = "walking";
						listViewOfPath.setAdapter(new ArrayAdapter<String>(PathListActivity.this, android.R.layout.simple_list_item_1, JSONParser.jInstructions));
						break;
				}// End of switch

				MainActivity.mMap.clear();
				MainActivity.toMarker = MainActivity.mMap.addMarker(MainActivity.toMarkerOpt);
				MainActivity.mPathHelper.getPath(MainActivity.nowLatLng, MainActivity.toLatLng);
				finish();
			}// End of onCheckedChanged
		});

		listViewOfPath = (ListView) findViewById(R.id.listViewOfPath);

		if (JSONParser.jInstructions != null) {
			listViewOfPath.setAdapter(new ArrayAdapter<String>(PathListActivity.this, android.R.layout.simple_list_item_1, JSONParser.jInstructions));
		}// End of if-condition

		buttonOfRemove = (Button) findViewById(R.id.buttonOfRemove);
		buttonOfRemove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.guiding = false;
				MainActivity.timerOfGuide.cancel();
				MainActivity.mMap.clear();
				MainActivity.toMarker = null;
				MainActivity.textOfMapDescription.setText("");
				MainActivity.nowLatLng = MainActivity.mMapHelper.presentLatLng();

				MainActivity.mMapHelper.setRatio(15);
				MainActivity.mMapHelper.setBearing(0);
				MainActivity.mMapHelper.setTilt(30);
				MainActivity.mMapHelper.updateMap(MainActivity.nowLatLng.latitude, MainActivity.nowLatLng.longitude);
				finish();
				Toast.makeText(PathListActivity.this, R.string.toast_NavigationModeClose, Toast.LENGTH_SHORT).show();
			}// End of onClick
		});

		buttonOfExit = (Button) findViewById(R.id.buttonOfExit);
		buttonOfExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}// End of onClick
		});
	}// End of onCreate
}// End of PathListActivity
