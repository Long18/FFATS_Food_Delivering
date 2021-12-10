package shipper.william.ffats.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.Locale;

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

    public static Shipper isShipper;

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
        else
            return "Shipping";

    }
}
