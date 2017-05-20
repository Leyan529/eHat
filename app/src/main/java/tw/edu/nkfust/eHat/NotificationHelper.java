package tw.edu.nkfust.eHat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;

/**
 * Created by Leyan on 2017/5/20.
 */

public class NotificationHelper {
    private Context context;
    private Notification notify;
    public static int NOTIFY_MAIN = 1;


    public NotificationHelper(Context context) {
        this.context = context;
    }

    public Notification mainNotify(Bitmap bmp) {
        Intent intent = new Intent(context,this.context.getClass());
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        /**getActivity(Context , 辨識碼 , Intent物件 , PendingIntent處理模式)*/
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notify = new Notification.Builder(context)
                .setTicker("eHat啟動中...")
                .setSmallIcon(R.drawable.notifyp32, 0)
                .setContentTitle("eHat")
                .setContentText("回到主頁面")
                .setLargeIcon(bmp)
                .setWhen(System.currentTimeMillis())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .build();
        return notify;
    }
}
