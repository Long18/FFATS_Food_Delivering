package client.william.ffats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import client.william.ffats.Account.activity_sign_in;
import client.william.ffats.Account.activity_sign_up;
import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;
    TextView txtSlogan;

    FirebaseDatabase db;
    DatabaseReference table_user;

    String rmbNumberPhone, rmbPassword, rmbCodeCountry;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region btnSignIn
            if (v.getId() == R.id.btnSignIn){
                startActivity(new Intent(MainActivity.this, activity_sign_in.class));
            }
            //endregion
            //region btnSignUp
            if (v.getId() == R.id.btnSignUp){
                startActivity(new Intent(MainActivity.this, activity_sign_up.class));
            }
            //endregion
        }
    };

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insertData();
        viewConstructor();

    }

    private void insertData() {
        db = FirebaseDatabase.getInstance();
        table_user = db.getReference("user");
    }

    private void viewConstructor() {
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtSlogan = findViewById(R.id.txtSlogan);

        Paper.init(this);

        btnSignIn.setOnClickListener(onClickListener);
        btnSignUp.setOnClickListener(onClickListener);

        //Auto login when already click remember button
        rmbNumberPhone = Paper.book().read(Common.USER_KEY);
        rmbPassword = Paper.book().read(Common.PWD_KEY);
        rmbCodeCountry = Paper.book().read(Common.CCP_KEY);

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        if(sessionManager.checkUserLogin()){
            if (rmbNumberPhone != null && rmbPassword != null) {
                if (!rmbNumberPhone.isEmpty() && !rmbPassword.isEmpty()) {
                    login(rmbNumberPhone, rmbPassword, rmbCodeCountry);
                }
            }
        }

    }
    //endregion

    //region Function
    private void login(String phone, String pwd,String cpp) {
        if(Common.isConnectedToInternet(getBaseContext())) {

            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please wait...");
            Drawable drawable = new ProgressBar(MainActivity.this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN);
            mDialog.setIndeterminateDrawable(drawable);
            mDialog.show();

            String getUserPhoneNumber = phone;// Get Phone Num

            if (getUserPhoneNumber.charAt(0) == '0') {
                getUserPhoneNumber = getUserPhoneNumber.substring(1);
            }

            final String phoneNo = "+" + cpp + getUserPhoneNumber;

            //Database
            Query checkUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone").equalTo(phoneNo);

            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {


                        String systemPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                        if (systemPassword.equals(pwd)) {


                            startActivity(new Intent(MainActivity.this, Home.class));
                            finish();

                            String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                            String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                            String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                            //Create Database Store
                            SessionManager sessionManager = new SessionManager(MainActivity.this, SessionManager.SESSION_USER);
                            sessionManager.createLoginSession(mName, mPhoneNo, mPassword);

                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                        } else {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Toast.makeText(MainActivity.this, "Please check internet", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    //endregion
}