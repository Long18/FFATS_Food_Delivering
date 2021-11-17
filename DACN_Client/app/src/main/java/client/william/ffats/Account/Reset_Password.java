package client.william.ffats.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.rey.material.widget.CheckBox;

import client.william.ffats.R;

public class Reset_Password extends AppCompatActivity {
    //region Declare Variable
    TextInputLayout txtInPhoneNumber;
    Button btnContinue;

    CountryCodePicker countryNumber;

    boolean phoneCheck = false;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region btnContinue
            if (v.getId() == R.id.resetPassword_btnContinue){
                //get data
                String mPhoneNumber = txtInPhoneNumber.getEditText().getText().toString().trim();
                if (mPhoneNumber.charAt(0) == '0'){
                    mPhoneNumber = mPhoneNumber.substring(1);
                }
                final String getPhoneNumber = "+" + countryNumber.getFullNumber() + mPhoneNumber;


                //Database
                Query checkUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone").equalTo(getPhoneNumber);
                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            txtInPhoneNumber.setError(null);

                            Intent intent = new Intent(getApplicationContext(),Verify_OTP.class);

                            intent.putExtra("phone",getPhoneNumber);
                            intent.putExtra("ToDO","updateData");
                            intent.putExtra("Pass","updateData");
                            startActivity(intent);

                            finish();
                        }else {
                            txtInPhoneNumber.setError("Tài khoản không tồn tại");
                            txtInPhoneNumber.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Reset_Password.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //endregion
        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        viewConstructor();
    }

    private void viewConstructor() {
        txtInPhoneNumber = findViewById(R.id.txtLayoutPhone);
        countryNumber = findViewById(R.id.countryNumber);
        btnContinue = findViewById(R.id.resetPassword_btnContinue);

        txtInPhoneNumber.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // check
                String result = Validate.validatePhoneNumber(txtInPhoneNumber.getEditText().getText().toString());
                if (result != null){
                    txtInPhoneNumber.setError(result);
                    phoneCheck = false;
                }else{
                    txtInPhoneNumber.setError(null);
                    phoneCheck = true;
                }
                if (phoneCheck){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });

        btnContinue.setOnClickListener(onClickListener);

    }
    //endregion
}