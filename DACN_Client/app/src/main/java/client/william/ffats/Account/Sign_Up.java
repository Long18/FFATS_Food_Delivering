package client.william.ffats.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import client.william.ffats.R;

public class Sign_Up extends AppCompatActivity {
    //region Declare Variable
    TextInputLayout txtInPhoneNumber, txtInFullName, txtInLastName;
    TextView txtLoginAccount;
    CountryCodePicker countryNumber;
    Button btnContinue;

    boolean phoneCheck = false;
    boolean fullNameCheck = false;
    boolean lastNameCheck = false;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Sign Up
            if (v.getId() == R.id.signUp_btnContinue){
                String getUserPhoneNumber = txtInPhoneNumber.getEditText().getText().toString().trim();// Get Phone Num
                String getFName = txtInFullName.getEditText().getText().toString().trim();// Get First Name
                String getLName = txtInLastName.getEditText().getText().toString().trim();// Get Last Name

                String fullName = getLName + " " + getFName;

                if (getUserPhoneNumber.charAt(0) == '0') {
                    getUserPhoneNumber = getUserPhoneNumber.substring(1);
                }

                final String phoneNo = "+" + countryNumber.getFullNumber() + getUserPhoneNumber;

                Intent intent = new Intent(getApplicationContext(), Verify_OTP.class);

                intent.putExtra("phone", phoneNo); //Truyền chuỗi số điện thoại qua OTP activity
                intent.putExtra("name", fullName);
                intent.putExtra("ToDO", "createNewUser");
                intent.putExtra("Pass", "createNewUser");

                startActivity(intent);
                finish();
            }
            //endregion
            //region Login
            if (v.getId() == R.id.txtLoginAccount) {
                startActivity(new Intent(Sign_Up.this, Sign_In.class));
            }
            //endregion
        }
    };
    //endregion

    //region Function Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        viewConstructor();
    }

    private void viewConstructor() {
        txtInPhoneNumber = findViewById(R.id.txtLayoutPhone);
        txtInFullName = findViewById(R.id.txtLayoutFName);
        txtInLastName = findViewById(R.id.txtLayoutLName);
        txtLoginAccount = findViewById(R.id.txtLoginAccount);
        countryNumber = findViewById(R.id.countryNumber);
        btnContinue = findViewById(R.id.signUp_btnContinue);

        txtInPhoneNumber.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = Validate.validatePhoneNumber(txtInPhoneNumber.getEditText().getText().toString());
                if (result != null){
                    txtInPhoneNumber.setError(result);
                    phoneCheck = false;
                }else{
                    txtInPhoneNumber.setError(null);
                    phoneCheck = true;
                }
                if (phoneCheck && fullNameCheck && lastNameCheck){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });
        txtInFullName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = Validate.validateInput(txtInFullName.getEditText().getText().toString());
                if (result != null){
                    txtInFullName.setError(result);
                    fullNameCheck = false;
                }else{
                    txtInFullName.setError(null);
                    fullNameCheck = true;
                }
                if (phoneCheck && fullNameCheck && lastNameCheck){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });
        txtInLastName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = Validate.validateInput(txtInLastName.getEditText().getText().toString());
                if (result != null){
                    txtInLastName.setError(result);
                    lastNameCheck = false;
                }else{
                    txtInLastName.setError(null);
                    lastNameCheck = true;
                }
                if (phoneCheck && fullNameCheck && lastNameCheck){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });

        btnContinue.setOnClickListener(onClickListener);
        txtLoginAccount.setOnClickListener(onClickListener);
    }
    //endregion

}