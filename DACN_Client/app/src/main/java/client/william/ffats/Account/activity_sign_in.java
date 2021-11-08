package client.william.ffats.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPhone = findViewById(R.id.txtEditTextPhone);
        edtPassword = findViewById(R.id.txtEditTextPassword);
        btnSignIn = findViewById(R.id.btnContinue);
        ckbRemember = findViewById(R.id.ckbRemember);

        Paper.init(this);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = db.getReference("user");


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Common.isConnectedToInternet(getBaseContext())) {

                    //Remember User
                    if (ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,editPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(activity_sign_in.this);
                    mDialog.setMessage("Please wait...");
                    Drawable drawable = new ProgressBar(activity_sign_in.this).getIndeterminateDrawable().mutate();
                    drawable.setColorFilter(ContextCompat.getColor(activity_sign_in.this, R.color.colorPrimary),
                            PorterDuff.Mode.SRC_IN);
                    mDialog.setIndeterminateDrawable(drawable);
                    mDialog.show();

                    //Database
                    Query checkUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone").equalTo(editPhone.getText().toString());

                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                editPhone.setError(null);

                                String systemPassword = snapshot.child(editPhone.getText().toString()).child("password").getValue(String.class);
                                if (systemPassword.equals(edtPassword.getText().toString())) {
                                    edtPassword.setError(null);

                                    startActivity(new Intent(activity_sign_in.this, Home.class));
                                    finish();

                                    String mName = snapshot.child(editPhone.getText().toString()).child("name").getValue(String.class);
                                    String mPhoneNo = snapshot.child(editPhone.getText().toString()).child("phone").getValue(String.class);
                                    String mPassword = snapshot.child(editPhone.getText().toString()).child("password").getValue(String.class);

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
        });

    }
}