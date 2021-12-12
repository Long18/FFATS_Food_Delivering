package shipper.william.ffats.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import shipper.william.ffats.Model.LocationShipper;
import shipper.william.ffats.Model.Request;
import shipper.william.ffats.Model.Shipper;

public class Common {
    public static final int CHOOSE_IMAGE_REQUEST = 18;

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static final String DELETE = "Delete";
    public static final String UPDATE = "Update";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String CCP_KEY = "CountryCodePicker";


    public static String KEY_REALTIME;

    public static Shipper isShipper;
    public static Request currentRequest;

    public static String resSelected = "";

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap scaleBitmap = Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);

        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaleY = newHeight/(float)bitmap.getHeight();
        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaleBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaleBitmap;
    }

    public static String getDate(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(android.text.format.DateFormat.format("dd-MM-yyyy HH:mm",calendar).toString());
        return date.toString();
    }

    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else if (status.equals("2"))
            return "Shipping";
        else
            return "Shipped";

    }

    public static void createLocationRealTime(String key, String phone, Location mLastLocation){
        LocationShipper shipperLocation = new LocationShipper();
        shipperLocation.setOrderId(key);
        shipperLocation.setShipperPhone(phone);
        shipperLocation.setLat(mLastLocation.getLatitude());
        shipperLocation.setLng(mLastLocation.getLongitude());

        FirebaseDatabase.getInstance().getReference("LocationRealTime")
                .child(key)
                .setValue(shipperLocation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Bug",e.getMessage());
                    }
                });
    }

    public static void updateLocationRealTime(String key, Location mLastLocation){
        Map<String,Object> mRealTimeLocation = new HashMap<>();
        mRealTimeLocation.put("lat",mLastLocation.getLatitude());
        mRealTimeLocation.put("lng",mLastLocation.getLongitude());

        FirebaseDatabase.getInstance().getReference("LocationRealTime")
                .child(key)
                .updateChildren(mRealTimeLocation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Bug",e.getMessage());
                    }
                });
    }
}
