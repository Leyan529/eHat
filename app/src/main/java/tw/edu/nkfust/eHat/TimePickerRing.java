package tw.edu.nkfust.eHat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class TimePickerRing extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!MainActivity.mRFduinoManager.isOutOfRange()) MainActivity.mRFduinoManager.onStateChanged(8);

		new AlertDialog.Builder(TimePickerRing.this)
				.setMessage(R.string.message_TimeIsUpOfTimePicker)
				.setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!MainActivity.mRFduinoManager.isOutOfRange()) MainActivity.mRFduinoManager.onStateChanged(9);

						finish();
					}// End of onClick
				})
				.show();
	}// End of onCreate
}// End of TimePickerRing
