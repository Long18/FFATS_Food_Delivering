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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.rey.material.widget.CheckBox;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Home;
import client.william.ffats.Model.User;
import client.william.ffats.R;
import io.paperdb.Paper;

public class activity_sign_in extends AppCompatActivity {
    TextInputEditText editPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;

    CountryCodePicker countryNumber;

    boolean phoneCheck = false;
    boolean passCheck = false;


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region btn SignIn
            if (v.getId() == R.id.signIn_btnContinue){
                if(Common.isConnectedToInternet(getBaseContext())) {

                    //Remember User
                    if (ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,editPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                        Paper.book().write(Common.CCP_KEY,countryNumber.getFullNumber());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(activity_sign_in.this);
                    mDialog.setMessage("Please wait...");
                    Drawable drawable = new ProgressBar(activity_sign_in.this).getIndeterminateDrawable().mutate();
                    drawable.setColorFilter(ContextCompat.getColor(activity_sign_in.this, R.color.colorPrimary),
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

                                    startActivity(new Intent(activity_sign_in.this, Home.class));
                                    finish();

                                    String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                                    String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                                    String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                                    //Create Database Store
                                    SessionManager sessionManager = new SessionManager(activity_sign_in.this, SessionManager.SESSION_USER);
                                    sessionManager.createLoginSession(mName, mPhoneNo, mPassword);

                                    mDialog.dismiss();
                                    Toast.makeText(activity_sign_in.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(activity_sign_in.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(activity_sign_in.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(activity_sign_in.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(activity_sign_in.this, "Please check internet", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            //endregion
        }
    };

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    //region Function
    public void Register(View View) {
        startActivity(new Intent(activity_sign_in.this, activity_sign_up.class));
    }
    //endregion

}