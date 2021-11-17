package client.william.ffats.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.rey.material.widget.CheckBox;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Home;
import client.william.ffats.R;
import io.paperdb.Paper;

public class Sign_In extends AppCompatActivity {
    //region Declare Variable
    TextInputEditText editPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    TextView txtResetPassword,txtCreateAccount;

    CountryCodePicker countryNumber;

    boolean phoneCheck = false;
    boolean passCheck = false;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Sign In Auto
            if (v.getId() == R.id.signIn_btnContinue){
                if(Common.isConnectedToInternet(getBaseContext())) {

                    //Remember User
                    if (ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,editPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                        Paper.book().write(Common.CCP_KEY,countryNumber.getFullNumber());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(Sign_In.this);
                    mDialog.setMessage("Please wait...");
                    Drawable drawable = new ProgressBar(Sign_In.this).getIndeterminateDrawable().mutate();
                    drawable.setColorFilter(ContextCompat.getColor(Sign_In.this, R.color.colorPrimary),
                            PorterDuff.Mode.SRC_IN);
                    mDialog.setIndeterminateDrawable(drawable);
                    mDialog.show();

                    String getUserPhoneNumber = editPhone.getText().toString().trim();// Get Phone Num

                    if (getUserPhoneNumber.charAt(0) == '0') {
                        getUserPhoneNumber = getUserPhoneNumber.substring(1);
                    }

                    final String phoneNo = "+" + countryNumber.getFullNumber() + getUserPhoneNumber;

                    //Database
                    Query checkUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone").equalTo(phoneNo);

                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                editPhone.setError(null);

                                String systemPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                                if (systemPassword.equals(edtPassword.getText().toString())) {
                                    edtPassword.setError(null);

                                    startActivity(new Intent(Sign_In.this, Home.class));
                                    finish();

                                    String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                                    String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                                    String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                                    //Create Database Store
                                    SessionManager sessionManager = new SessionManager(Sign_In.this, SessionManager.SESSION_USER);
                                    sessionManager.createLoginSession(mName, mPhoneNo, mPassword);

                                    mDialog.dismiss();
                                    Toast.makeText(Sign_In.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(Sign_In.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Sign_In.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Sign_In.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(Sign_In.this, "Please check internet", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            //endregion
            //region Reset Password
            if (v.getId() == R.id.txtResetPassword){
                startActivity(new Intent(Sign_In.this, Reset_Password.class));
            }
            //endregion
            //region Sign In
            if (v.getId() == R.id.txtCreateAccount){
                startActivity(new Intent(Sign_In.this, Sign_Up.class));
            }
            //endregion

        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);

        viewConstructor();

    }

    private void viewConstructor(){

        Paper.init(this);

        editPhone = findViewById(R.id.txtEditTextPhone);
        edtPassword = findViewById(R.id.txtEditTextPassword);
        countryNumber = findViewById(R.id.countryNumber);
        btnSignIn = findViewById(R.id.signIn_btnContinue);
        ckbRemember = findViewById(R.id.ckbRemember);
        txtResetPassword = findViewById(R.id.txtResetPassword);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);


        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // check
                String result = Validate.validatePhoneNumber(editPhone.getText().toString());
                if (result != null){
                    editPhone.setError(result);
                    phoneCheck = false;
                }else{
                    editPhone.setError(null);
                    phoneCheck = true;
                }
                if (phoneCheck && passCheck){
                    btnSignIn.setEnabled(true);
                }else {
                    btnSignIn.setEnabled(false);
                }
            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // check
                String result = Validate.validatePassword(edtPassword.getText().toString());
                if (result != null){
                    edtPassword.setError(result);
                    passCheck = false;
                }else{
                    edtPassword.setError(null);
                    passCheck = true;
                }
                if (phoneCheck && passCheck){
                    btnSignIn.setEnabled(true);
                }else {
                    btnSignIn.setEnabled(false);
                }
            }
        });

        btnSignIn.setOnClickListener(onClickListener);
        txtResetPassword.setOnClickListener(onClickListener);
        txtCreateAccount.setOnClickListener(onClickListener);

    }
    //endregion

    //region Function
    public void Register(View View) {
        startActivity(new Intent(Sign_In.this, Sign_Up.class));
    }
    //endregion

}