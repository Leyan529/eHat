package tw.edu.nkfust.eHat;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.RingtoneManager;

/**
 * Created by Leyan on 2017/5/20.
 */

public class NotificationHelper {
    private Context context;
    private Notification notify;
    public static int NOTIFY_MAIN= 1;


    public NotificationHelper(Context context) {
        this.context = context;
    }
    public Notification mainNotify(Bitmap bmp){
        notify = new Notification.Builder(context)
                .setTicker("eHat啟動中...")
                .setSmallIcon(R.drawable.notifyp32,0)
                .setContentTitle("eHat")
                .setContentText("功能畫面")
                .setLargeIcon(bmp)
                .setWhen(System.currentTimeMillis())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        return notify;
    }
}
