package shipper.william.ffats.Account;

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
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;
import shipper.william.ffats.Common.Common;
import shipper.william.ffats.Database.SessionManager;
import shipper.william.ffats.Home;
import shipper.william.ffats.R;

public class Login extends AppCompatActivity {

    //region Declare Variable
    TextInputEditText editPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;

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
                        Paper.book().write(Common.CCP_KEY,"+84");
                    }


                    final ProgressDialog mDialog = new ProgressDialog(Login.this);
                    mDialog.setMessage("Please wait...");
                    Drawable drawable = new ProgressBar(Login.this).getIndeterminateDrawable().mutate();
                    drawable.setColorFilter(ContextCompat.getColor(Login.this, R.color.colorPrimary),
                            PorterDuff.Mode.SRC_IN);
                    mDialog.setIndeterminateDrawable(drawable);
                    mDialog.show();

                    String getUserPhoneNumber = editPhone.getText().toString().trim();// Get Phone Num

                    if (getUserPhoneNumber.charAt(0) == '0') {
                        getUserPhoneNumber = getUserPhoneNumber.substring(1);
                    }

                    final String phoneNo = "+84" + getUserPhoneNumber;

                    //Database
                    Query checkUser = FirebaseDatabase.getInstance().getReference("Shippers").orderByChild("phone").equalTo(phoneNo);

                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                editPhone.setError(null);

                                String systemPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                                if (systemPassword.equals(edtPassword.getText().toString())) {
                                    edtPassword.setError(null);

                                    startActivity(new Intent(Login.this, Home.class));
                                    finish();

                                    String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                                    String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                                    String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);
                                    String mImage = snapshot.child(phoneNo).child("image").getValue(String.class);
                                    String mSumOrders = snapshot.child(phoneNo).child("sumOrders").getValue(String.class);

                                    //Create Database Store
                                    SessionManager sessionManager = new SessionManager(Login.this, SessionManager.SESSION_USER);
                                    sessionManager.createLoginSession(mName, mPhoneNo, mPassword,mImage,mSumOrders);

                                    mDialog.dismiss();
                                    Toast.makeText(Login.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(Login.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Login.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(Login.this, "Please check internet", Toast.LENGTH_SHORT).show();
                    return;
                }

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
        setContentView(R.layout.activity_login);
        viewConstructor();
    }

    private void viewConstructor() {
        Paper.init(this);

        editPhone = findViewById(R.id.txtEditTextPhone);
        edtPassword = findViewById(R.id.txtEditTextPassword);
        btnSignIn = findViewById(R.id.signIn_btnContinue);
        ckbRemember = findViewById(R.id.ckbRemember);


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
    }
    //endregion
}