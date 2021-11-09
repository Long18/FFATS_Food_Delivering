package server.william.ffats.Database;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences userSession;
    SharedPreferences.Editor editor;
    Context context;

    public static final String SESSION_USER = "userLoginSession";
    private static final String IS_LOGIN = "UserIsLogin";

    //User Store Information
    public static final String KEY_FULLNAME = "name";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONENUMBER = "phone";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_DATE = "date";
    public static final String KEY_GENDER = "gender";

    public SessionManager(Context mContext, String sessionName) {
        context = mContext;
        userSession = context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
        editor = userSession.edit();
    }

    public void createLoginSession(String name, String phone, String password) {

        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_FULLNAME, name);
        editor.putString(KEY_PHONENUMBER, phone);
        editor.putString(KEY_PASSWORD, password);

        editor.commit();

    }

    public HashMap<String, String> getInfomationUser() {
        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(KEY_FULLNAME, userSession.getString(KEY_FULLNAME, null));
        userData.put(KEY_PHONENUMBER, userSession.getString(KEY_PHONENUMBER, null));
        userData.put(KEY_PASSWORD, userSession.getString(KEY_PASSWORD, null));

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
        editor.putString(KEY_PHONENUMBER, null);
        editor.putString(KEY_PASSWORD, null);
        editor.putString(KEY_DATE, null);
        editor.putString(KEY_GENDER, null);

        userSession.getBoolean(IS_LOGIN,false);

        editor.commit();

    }
}
