package server.william.ffats.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.Locale;

import server.william.ffats.Model.Request;
import server.william.ffats.Model.User;
import server.william.ffats.Remote.APIService;
import server.william.ffats.Remote.FCMRetrofitClient;
import server.william.ffats.Remote.IGeoCoordinates;
import server.william.ffats.Remote.RetrofitClient;


public class Common {
    public static String resSelected = "";

    public static User currentUser;
    public static Request currentRequest;

    public static final String DELETE = "Delete";
    public static final String UPDATE = "Update";


    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String CCP_KEY = "+84";

    public static final int CHOOSE_IMAGE_REQUEST = 18;

    public static final String baseUrl = "https://maps.googleapis.com";

    private static final String DCM_URL = "https://fcm.googleapis.com/";

    public static APIService getGCMService(){
        return FCMRetrofitClient.getClient(DCM_URL).create(APIService.class);
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


    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth, int newHeight){
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

    public static String getDate(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(android.text.format.DateFormat.format("dd-MM-yyyy HH:mm",calendar).toString());
        return date.toString();
    }
}
