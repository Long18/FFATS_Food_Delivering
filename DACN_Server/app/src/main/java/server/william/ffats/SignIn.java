package server.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.ProgressDialog;
import android.content.Context;
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

import server.william.ffats.Common.Common;
import server.william.ffats.Database.SessionManager;
import server.william.ffats.Model.User;


public class SignIn extends AppCompatActivity {

    TextInputEditText editPhone, edtPassword;
    Button btnSignIn;

    FirebaseDatabase db;
    DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPhone = findViewById(R.id.txtEditTextPhone);
        edtPassword = findViewById(R.id.txtEditTextPassword);
        btnSignIn = findViewById(R.id.btnContinue);

        db = FirebaseDatabase.getInstance();
        table_user = db.getReference("user");


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Please wait...");
                Drawable drawable = new ProgressBar(SignIn.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(SignIn.this, R.color.colorPrimary),
                        PorterDuff.Mode.SRC_IN);
                mDialog.setIndeterminateDrawable(drawable);
                mDialog.show();

                String getUserPhoneNumber = editPhone.getText().toString().trim();// Get Phone Num

                if (getUserPhoneNumber.charAt(0) == '0') {
                    getUserPhoneNumber = getUserPhoneNumber.substring(1);
                }

                final String phoneNo = "+84"  + getUserPhoneNumber;

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

                                startActivity(new Intent(SignIn.this, Home.class));
                                finish();

                                String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                                String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                                String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                                //Create Database Store
                                SessionManager sessionManager = new SessionManager(SignIn.this, SessionManager.SESSION_USER);
                                sessionManager.createLoginSession(mName, mPhoneNo, mPassword);

                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(SignIn.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignIn.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

//                table_user.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        if(dataSnapshot.child(editPhone.getText().toString()).exists()){
//
//                            mDialog.dismiss();
//                            User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
//                            user.setPhone(editPhone.getText().toString());
//                            if(user.getPassword().equals(edtPassword.getText().toString()))
//                            {
//                                {
//                                    Intent homeInten = new Intent(SignIn.this,Home.class);
//                                    Common.currentUser = user;
//                                    startActivity(homeInten);
//                                    finish();
//                                    Toast.makeText(SignIn.this,"OK",Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            else
//                            {
//                                Toast.makeText(SignIn.this,"Wrong password",Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        else
//                        {
//                            mDialog.dismiss();
//                            Toast.makeText(SignIn.this,"Not Found",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
            }
        });

    }
}