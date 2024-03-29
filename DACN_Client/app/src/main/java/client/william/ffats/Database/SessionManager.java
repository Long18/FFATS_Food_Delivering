package client.william.ffats.Database;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import client.william.ffats.Maps.MapValue;

public class SessionManager {
    SharedPreferences userSession;
    SharedPreferences.Editor editor;
    Context context;

    public static final String SESSION_USER = "userLoginSession";
    private static final String IS_LOGIN = "UserIsLogin";

    public static MapValue MAP_VALUE;

    //User Store Information
    public static final String KEY_FULLNAME = "name";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_IMAGE = null;
    public static final String KEY_EMAIL = "null";
    public static final String KEY_PHONENUMBER = "phone";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_DATE = "date";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_CITY = "Biên Hoà";
    public static final String KEY_ADDRESS = "Việt Nam";

    public SessionManager(Context mContext, String sessionName) {
        context = mContext;
        userSession = context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
        editor = userSession.edit();
    }

    public void createLocation(String city) {
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_CITY, city);

        editor.commit();

    }

    public void createAddress(String address) {
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_ADDRESS, address);

        editor.commit();

    }

    public void createImage(String image) {
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_IMAGE, image);

        editor.commit();

    }

    public void createEmail(String email) {
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_EMAIL, email);

        editor.commit();

    }

    public void createLoginSession(String name, String phone, String password,String image,String address,String email) {

        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_FULLNAME, name);
        editor.putString(KEY_PHONENUMBER, phone);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_IMAGE, image);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_EMAIL, email);

        editor.commit();

    }

    public HashMap<String, String> getInfomationUser() {
        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(KEY_FULLNAME, userSession.getString(KEY_FULLNAME, null));
        userData.put(KEY_PHONENUMBER, userSession.getString(KEY_PHONENUMBER, null));
        userData.put(KEY_IMAGE, userSession.getString(KEY_IMAGE, null));
        userData.put(KEY_EMAIL, userSession.getString(KEY_EMAIL, null));
        userData.put(KEY_PASSWORD, userSession.getString(KEY_PASSWORD, null));
        userData.put(KEY_CITY, userSession.getString(KEY_CITY, null));
        userData.put(KEY_ADDRESS, userSession.getString(KEY_ADDRESS, null));

        return userData;
    }

    public boolean checkUserLogin() {
        if (userSession.getBoolean(IS_LOGIN, false)) {
            return true;
        } else {
            return false;
        }
    }

    public void checkUserLogout() {
        editor.clear();

        editor.putString(KEY_FULLNAME, null);
        editor.putString(KEY_USERNAME, null);
        editor.putString(KEY_EMAIL, null);
        editor.putString(KEY_IMAGE, null);
        editor.putString(KEY_PHONENUMBER, null);
        editor.putString(KEY_PASSWORD, null);
        editor.putString(KEY_DATE, null);
        editor.putString(KEY_GENDER, null);
        editor.putString(KEY_CITY, null);
        editor.putString(KEY_ADDRESS, null);

        userSession.getBoolean(IS_LOGIN,false);

        editor.commit();

    }
}
