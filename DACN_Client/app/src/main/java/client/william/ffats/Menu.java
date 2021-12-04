package client.william.ffats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import client.william.ffats.Account.Information;
import client.william.ffats.Database.SessionManager;
import io.paperdb.Paper;

public class Menu extends AppCompatActivity {

    //region Declare Variable
    TextView txtLogout,txtFullName;
    LinearLayout lnlFavorites, lnlAddress, lnlPayment, lnlPromote, lnlOrder, lnlSupport, lnlSetting;
    ImageView btnDarkMode,btnAvata;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    SharedPreferences sharedPreferences;
    Boolean isNightMode;

    FrameLayout frlUser;


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Darkmode
            if (v.getId() == R.id.ic_darkmode) {

                if (isNightMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    btnDarkMode.setImageResource(R.drawable.nights_stay_on);
                }else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    btnDarkMode.setImageResource(R.drawable.nights_stay_off);
                }

            }
            //endregion
            //region Fav
            if (v.getId() == R.id.lnl_fav) {
                Intent openMenu = new Intent(Menu.this, FavoritesActivity.class);
                startActivity(openMenu);

            }
            //endregion
            //region Address
            if (v.getId() == R.id.lnl_address) {
//                 Intent openMenu = new Intent(Menu.this, FavoritesActivity.class);
//                 startActivity(openMenu);

            }
            //endregion
            //region Payment
            if (v.getId() == R.id.lnl_payment) {
                Intent payment = new Intent(Menu.this, Cart.class);
                startActivity(payment);

            }
            //endregion
            //region Promotion
            if (v.getId() == R.id.lnl_promotion) {
                Intent openMenu = new Intent(Menu.this, FavoritesActivity.class);
                startActivity(openMenu);

            }
            //endregion
            //region Order Status
            if (v.getId() == R.id.lnl_order) {
                Intent orderstatus = new Intent(Menu.this, OrderStatus.class);
                startActivity(orderstatus);

            }
            //endregion
            //region Support
            if (v.getId() == R.id.lnl_support) {
//                 Intent openMenu = new Intent(Menu.this, FavoritesActivity.class);
//                 startActivity(openMenu);

            }
            //endregion
            //region Setting
            if (v.getId() == R.id.lnl_setting) {
//                Intent openMenu = new Intent(Menu.this, FavoritesActivity.class);
//                startActivity(openMenu);

            }
            //endregion
            //region Log out
            if (v.getId() == R.id.txtLogout) {

                SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
                sessionManager.checkUserLogout();

                Paper.book().destroy();

                Intent mainActivity = new Intent(Menu.this, MainActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);

            }
            //endregionfrlUser
            //region frame layout User
            if (v.getId() == R.id.frlUser) {

                Intent infor = new Intent(Menu.this, Information.class);
                startActivity(infor);

            }
            //endregion
        }
    };
    //endregion

    //region Function Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        insertData();
        viewConstructor();
    }

    private void insertData() {
        Paper.init(this);
    }

    private void viewConstructor() {
        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        sharedPreferences = getSharedPreferences("Darkmode",0);
        isNightMode = sharedPreferences.getBoolean("Nightmode",false);

        lnlFavorites = findViewById(R.id.lnl_fav);
        lnlAddress = findViewById(R.id.lnl_address);
        lnlPayment = findViewById(R.id.lnl_payment);
        lnlPromote = findViewById(R.id.lnl_promotion);
        lnlOrder = findViewById(R.id.lnl_order);
        lnlSupport = findViewById(R.id.lnl_support);
        lnlSetting = findViewById(R.id.lnl_setting);
        txtLogout = findViewById(R.id.txtLogout);
        txtFullName = findViewById(R.id.txtFullName);
        frlUser = findViewById(R.id.frlUser);
        btnDarkMode = findViewById(R.id.ic_darkmode);
        btnAvata = findViewById(R.id.img_avata);


        txtFullName.setText(userInformation.get(SessionManager.KEY_FULLNAME));

        lnlFavorites.setOnClickListener(onClickListener);
        lnlAddress.setOnClickListener(onClickListener);
        lnlPayment.setOnClickListener(onClickListener);
        lnlPromote.setOnClickListener(onClickListener);
        lnlOrder.setOnClickListener(onClickListener);
        lnlSupport.setOnClickListener(onClickListener);
        lnlSetting.setOnClickListener(onClickListener);
        txtLogout.setOnClickListener(onClickListener);
        btnDarkMode.setOnClickListener(onClickListener);
        btnAvata.setOnClickListener(onClickListener);
        frlUser.setOnClickListener(onClickListener);


    }
    //endregion
}