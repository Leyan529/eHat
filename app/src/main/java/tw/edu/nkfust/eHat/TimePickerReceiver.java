package tw.edu.nkfust.eHat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimePickerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		intent.setClass(context, TimePickerRing.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}// End of onReceive
}// End of TimePickerReceiver
