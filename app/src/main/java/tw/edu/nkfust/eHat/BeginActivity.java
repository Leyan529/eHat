package tw.edu.nkfust.eHat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BeginActivity extends AppCompatActivity {
	private final int STATE_START = 0;
	private TextView textOfTime;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case STATE_START:
					startActivity(new Intent(BeginActivity.this, MainActivity.class));
					finish();
					break;
				default:
					break;
			}// End of switch-condition
		}// End of handleMessage
	};
	String time = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_begin);
		textOfTime = (TextView) findViewById(R.id.textOfTime);
		/*SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		Date date = new Date();*/
		textOfTime.setText(time);
		handler.sendEmptyMessageDelayed(STATE_START, 1000); // Delay 2s
	}// End of onCreate
}// End of BeginActivity
