package server.william.ffats.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import server.william.ffats.Database.SessionManager;
import server.william.ffats.Helper.NotificationHelper;
import server.william.ffats.OrderStatus;
import server.william.ffats.R;


public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        Log.e("NEW_TOKEN", s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            sendNotificationAPI(remoteMessage);
        }else {
            sendNotification(remoteMessage);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationAPI(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = notification.getTitle();
        String content = notification.getBody();

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();

        Intent intent = new Intent(this, OrderStatus.class);
        intent.putExtra("userPhone",userInformation.get(SessionManager.KEY_PHONENUMBER));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper helper = new NotificationHelper(this);
        Notification.Builder builder = helper.getFFASTChannel(title,content,pendingIntent,defaultSound);

        helper.getManager().notify(new Random().nextInt(), builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);
        Log.e("JSON_OBJECT", object.toString());

        String NOTIFICATION_CHANNEL_ID = "William_channel";

        long pattern[] = {0, 1000, 500, 1000};

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Your Notifications",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(pattern);
            notificationChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            channel.canBypassDnd();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(remoteMessage.getNotification().getBody())
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);


        mNotificationManager.notify(1000, notificationBuilder.build());
    }
}
