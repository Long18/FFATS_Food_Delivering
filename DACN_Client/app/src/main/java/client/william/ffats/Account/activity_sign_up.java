package client.william.ffats.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import client.william.ffats.R;

public class activity_sign_up extends AppCompatActivity {

    TextInputLayout phoneNumber, fName,lName;
    CountryCodePicker countryNumber;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        phoneNumber = findViewById(R.id.txtLayoutPhone);
        fName = findViewById(R.id.txtLayoutFName);
        lName = findViewById(R.id.txtLayoutLName);
        countryNumber = findViewById(R.id.countryNumber);
        btnNext = findViewById(R.id.btnContinue);

//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference table_user = database.getReference("user");
//
//        btnNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (Common.isConnectedToInternet(getBaseContext())) {
//
//                    final ProgressDialog mDialog = new ProgressDialog(activity_sign_up.this);
//
//                    mDialog.setMessage("Please wait...");
//                    Drawable drawable = new ProgressBar(activity_sign_up.this).getIndeterminateDrawable().mutate();
//                    drawable.setColorFilter(ContextCompat.getColor(activity_sign_up.this, R.color.colorPrimary),
//                            PorterDuff.Mode.SRC_IN);
//                    mDialog.setIndeterminateDrawable(drawable);
//                    mDialog.show();
//
//                    table_user.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
//                                mDialog.dismiss();
//                                Toast.makeText(activity_sign_up.this, "Phone Number Already Register", Toast.LENGTH_SHORT).show();
//
//                            } else {
//                                mDialog.dismiss();
//                                User user = new User(editPhone.getText().toString());
//                                table_user.child(editPhone.getText().toString()).setValue(user);
//                                Toast.makeText(activity_sign_up.this, "Register Success", Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//                else {
//                    Toast.makeText(activity_sign_up.this, "Please check internet", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }
//        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validatePhoneNumber()) {
                    return;
                }

                String getUserPhoneNumber = phoneNumber.getEditText().getText().toString().trim();// Get Phone Num
                String getFName = fName.getEditText().getText().toString().trim();// Get First Name
                String getLName = lName.getEditText().getText().toString().trim();// Get Last Name

                String fullName = getFName + " " + getLName;

                if (getUserPhoneNumber.charAt(0) == '0') {
                    getUserPhoneNumber = getUserPhoneNumber.substring(1);
                }

                final String phoneNo = "+" + countryNumber.getFullNumber() + getUserPhoneNumber;

                Intent intent = new Intent(getApplicationContext(), Verify_OTP.class);

                intent.putExtra("phoneNo", phoneNo); //Truyền chuỗi số điện thoại qua OTP activity
                intent.putExtra("name", fullName);
                intent.putExtra("ToDO", "createNewUser");

                startActivity(intent);
            }
        });
    }

    private boolean validatePhoneNumber() {
        String val = phoneNumber.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            phoneNumber.setError("Số điện thoại không được để trống!");
            return false;
        }
        else {
            phoneNumber.setError(null);
            phoneNumber.setErrorEnabled(false);
            return true;
        }
    }

}