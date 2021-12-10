package client.william.ffats.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import client.william.ffats.Model.User;
import client.william.ffats.Remote.APIService;
import client.william.ffats.Remote.IGoogleService;
import client.william.ffats.Remote.RetrofitClient;

public class Common {
    public static final int CHOOSE_IMAGE_REQUEST = 18;
    public static User currentUser;

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static final String DELETE = "Delete";
    public static final String UPDATE = "Update";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String CCP_KEY = "CountryCodePicker";

    public static String resSelected = "";

    public static IGoogleService getGoogleMaps(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static APIService getGCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "Shipping";

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
}
