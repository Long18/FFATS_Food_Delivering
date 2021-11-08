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
import com.google.firebase.database.ValueEventListener;

import client.william.ffats.Account.activity_sign_in;
import client.william.ffats.Account.activity_sign_up;
import client.william.ffats.Common.Common;
import client.william.ffats.Model.User;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;
    TextView txtSlogan;

    FirebaseDatabase db;
    DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtSlogan = findViewById(R.id.txtSlogan);

        db = FirebaseDatabase.getInstance();
        table_user = db.getReference("user");

        Paper.init(this);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, activity_sign_in.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, activity_sign_up.class));
            }
        });

        //Check User remember
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if (user != null && pwd != null) {
            if (!user.isEmpty() && !pwd.isEmpty()) {
                //login(user,pwd);
            }
        }

    }

//    private void login(String phone, String pwd) {
//        if(Common.isConnectedToInternet(getBaseContext())) {
//
//
//            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
//            mDialog.setMessage("Please wait...");
//            Drawable drawable = new ProgressBar(MainActivity.this).getIndeterminateDrawable().mutate();
//            drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
//                    PorterDuff.Mode.SRC_IN);
//            mDialog.setIndeterminateDrawable(drawable);
//            mDialog.show();
//
//            table_user.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                    if (dataSnapshot.child(phone).exists()) {
//
//                        mDialog.dismiss();
//                        User user = dataSnapshot.child(phone).getValue(User.class);
//                        user.setPhone(phone);
//                        if (user.getPassword().equals(pwd)){
//                            {
//                                Intent homeInten = new Intent(MainActivity.this, Home.class);
//                                Common.currentUser = user;
//                                startActivity(homeInten);
//                                finish();
//                                Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        mDialog.dismiss();
//                        Toast.makeText(MainActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }else{
//            Toast.makeText(MainActivity.this, "Please check internet", Toast.LENGTH_SHORT).show();
//            return;
//        }
//    }
}