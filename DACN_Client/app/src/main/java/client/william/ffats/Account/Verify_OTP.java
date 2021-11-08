package client.william.ffats.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

    String phoneNumber,fullName,ToDO,codeSystem;

    TextView txtDescription;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);


        pinView = findViewById(R.id.pin_view);
        txtDescription = findViewById(R.id.txtDescription);
        btnContinue = findViewById(R.id.btnContinue);

        phoneNumber = getIntent().getStringExtra("phoneNo");
        fullName = getIntent().getStringExtra("name");
        ToDO = getIntent().getStringExtra("ToDO");

        txtDescription.setText("We texted you a verification code to your phone number: " +phoneNumber);

        sendCode(phoneNumber);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = pinView.getText().toString();
                if (!code.isEmpty()) {
                    verifyCode(code);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (ToDO.equals("updateData")) {
                                updateUser();
                                Toast.makeText(Verify_OTP.this, "Xác nhận lại thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                //inputUser();
                                Intent intent = new Intent(getApplicationContext(), New_Password.class);

                                intent.putExtra("phoneNo", phoneNumber); //Truyền chuỗi số điện thoại qua OTP activity
                                intent.putExtra("name", fullName);
                                intent.putExtra("ToDO", "createNewUser");

                                startActivity(intent);
                            }


                            Toast.makeText(Verify_OTP.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

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


    private void updateUser() {
        Intent intent = new Intent(getApplicationContext(),New_Password.class);
        intent.putExtra("phoneNo",phoneNumber);
        startActivity(intent);
        finish();
    }


}