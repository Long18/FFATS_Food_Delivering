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

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import client.william.ffats.MainActivity;
import client.william.ffats.R;

public class Verify_OTP extends AppCompatActivity {
    PinView pinView;

    String phoneNumber,fullName,ToDO,codeSystem,Pass;

    TextView txtDescription,txtCaption;
    Button btnContinue;

    Boolean numberOTPCheck = false;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Auto set OTP
            if (v.getId() == R.id.verifyOTP_btnContinue){
                String code = pinView.getText().toString();
                if (!code.isEmpty()) {
                    verifyCode(code);
                }
            }
            //endregion
        }
    };

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        importData();
        viewConstructor();


    }

    private void viewConstructor() {
        pinView = findViewById(R.id.pin_view);
        txtDescription = findViewById(R.id.txtDescription);
        txtCaption = findViewById(R.id.txtOTP);
        btnContinue = findViewById(R.id.verifyOTP_btnContinue);

        btnContinue.setOnClickListener(onClickListener);

        if (Pass.equals("updateData")){
            txtCaption.setText("Reset new password");
        }else{
            txtCaption.setText("Sign Up");
        }

        txtDescription.setText("We texted you a verification code to your phone number: " +phoneNumber);

        sendCode(phoneNumber);

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = Validate.validateInput(pinView.getText().toString());
                if (result != null){
                    numberOTPCheck = false;
                }else{
                    numberOTPCheck = true;
                }
                if (numberOTPCheck){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });
    }


    private void importData() {
        phoneNumber = getIntent().getStringExtra("phone");
        fullName = getIntent().getStringExtra("name");
        ToDO = getIntent().getStringExtra("ToDO");
        Pass = getIntent().getStringExtra("Pass");
    }
    //endregion


    //region Function
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (ToDO.equals("createNewUser")) {

                                inputUser();
                                Toast.makeText(Verify_OTP.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            } else if (Pass.equals("updateData")){
                                updateUser();
                                Toast.makeText(Verify_OTP.this, "Xác nhận lại thành công!", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(Verify_OTP.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(Verify_OTP.this, "Không thể xác thực, hãy thử lại!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60, TimeUnit.SECONDS,this,mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codeSystem = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(Verify_OTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

    public void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void inputUser() {
        Intent intent = new Intent(getApplicationContext(), New_Password.class);
        Bundle bundle = new Bundle();

        bundle.putString("phone", phoneNumber); //Truyền chuỗi số điện thoại qua OTP activity
        bundle.putString("name", fullName);
        bundle.putString("ToDO", "createNewUser");
        bundle.putString("Pass", "createNewUser");
        intent.putExtras(bundle);

        startActivity(intent);
        finish();
    }

    private void updateUser() {
        Intent intent = new Intent(getApplicationContext(),New_Password.class);
        Bundle bundle = new Bundle();

        bundle.putString("phone",phoneNumber);
        bundle.putString("ToDO", "updateData");
        bundle.putString("Pass", "updateData");
        intent.putExtras(bundle);

        startActivity(intent);
        finish();
    }

    //endregion


}