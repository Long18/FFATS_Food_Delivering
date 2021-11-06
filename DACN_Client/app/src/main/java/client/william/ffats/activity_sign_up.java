package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
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

public class activity_sign_up extends AppCompatActivity {

    TextInputEditText editPhone;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editPhone = findViewById(R.id.txtEditTextPhone);
        btnNext = findViewById(R.id.btnContinue);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("user");

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    final ProgressDialog mDialog = new ProgressDialog(activity_sign_up.this);

                    mDialog.setMessage("Please wait...");
                    Drawable drawable = new ProgressBar(activity_sign_up.this).getIndeterminateDrawable().mutate();
                    drawable.setColorFilter(ContextCompat.getColor(activity_sign_up.this, R.color.colorPrimary),
                            PorterDuff.Mode.SRC_IN);
                    mDialog.setIndeterminateDrawable(drawable);
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(activity_sign_up.this, "Phone Number Already Register", Toast.LENGTH_SHORT).show();

                            } else {
                                mDialog.dismiss();
                                User user = new User(editPhone.getText().toString());
                                table_user.child(editPhone.getText().toString()).setValue(user);
                                Toast.makeText(activity_sign_up.this, "Register Success", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(activity_sign_up.this, "Please check internet", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}