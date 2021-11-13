package client.william.ffats.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import client.william.ffats.R;

public class NotificationHelper extends ContextWrapper {

    private static  final String CHANNEL_ID = "client.william.ffats.William";
    private static  final String CHANNEL_NAME = "FFATS";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel williamChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        williamChannel.enableLights(false);
        williamChannel.enableVibration(true);
        williamChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(williamChannel);
    }

    public NotificationManager getManager() {
        if (manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getFFASTChannel(String title, String body, PendingIntent content, Uri sound){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(content)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(sound)
                .setAutoCancel(false);
    }
}
