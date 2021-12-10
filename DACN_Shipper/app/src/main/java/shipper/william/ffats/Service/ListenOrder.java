package shipper.william.ffats.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;

import shipper.william.ffats.Database.SessionManager;
import shipper.william.ffats.MainActivity;
import shipper.william.ffats.Model.Request;
import shipper.william.ffats.R;

public class ListenOrder extends Service implements ChildEventListener {

    FirebaseDatabase db;
    DatabaseReference orders;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(ListenOrder.this, SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        db = FirebaseDatabase.getInstance();
        orders = db.getReference("PendingOrders").child(userInformation.get(SessionManager.KEY_PHONENUMBER));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        orders.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public ListenOrder() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Request request = snapshot.getValue(Request.class);
        if (request.getStatus().equals("2"))
            showNotification(snapshot.getKey(),request);
    }

    private void showNotification(String key, Request request) {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getBaseContext(), "foodStatus");
        Intent ii = new Intent(getBaseContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, ii, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        //bigText.bigText(verseurl);
        bigText.setBigContentTitle("Your order was updated");
        bigText.setSummaryText("You have new order #" +key);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Your order was updated");
        mBuilder.setContentText("You have new order #" +key);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "foodStatus";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "foodStatus",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        //Get random id
        int randomInt = new Random().nextInt(9999-1)+1;
        mNotificationManager.notify(randomInt, mBuilder.build());
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}