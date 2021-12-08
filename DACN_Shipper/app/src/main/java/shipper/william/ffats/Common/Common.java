package shipper.william.ffats.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Common {
    public static final int CHOOSE_IMAGE_REQUEST = 18;

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static final String DELETE = "Delete";
    public static final String UPDATE = "Update";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String CCP_KEY = "CountryCodePicker";

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
}
