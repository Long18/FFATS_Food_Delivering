package client.william.ffats;

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
import com.google.firebase.database.ValueEventListener;

import client.william.ffats.Common.Common;
import client.william.ffats.Model.User;

public class activity_sign_in extends AppCompatActivity {
    TextInputEditText editPhone, edtPassword;
    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPhone = findViewById(R.id.txtEditTextPhone);
        edtPassword = findViewById(R.id.txtEditTextPassword);
        btnSignIn = findViewById(R.id.btnContinue);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = db.getReference("user");


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(activity_sign_in.this);
                mDialog.setMessage("Please wait...");
                Drawable drawable = new ProgressBar(activity_sign_in.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(activity_sign_in.this, R.color.colorPrimary),
                        PorterDuff.Mode.SRC_IN);
                mDialog.setIndeterminateDrawable(drawable);
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child(editPhone.getText().toString()).exists()){

                            mDialog.dismiss();
                            User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                            //user.setPhone(editPhone.getText().toString());
                            if(user.getPassword().equals(edtPassword.getText().toString()))
                            {
                                {
                                    Intent homeInten = new Intent(activity_sign_in.this,Home.class);
                                    Common.currentUser = user;
                                    startActivity(homeInten);
                                    finish();
                                    Toast.makeText(activity_sign_in.this,"OK",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(activity_sign_in.this,"Wrong password",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            //mDialog.dismiss();
                            Toast.makeText(activity_sign_in.this,"Not Found",Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }
}