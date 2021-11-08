package client.william.ffats.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import client.william.ffats.Home;
import client.william.ffats.MainActivity;
import client.william.ffats.Model.User;
import client.william.ffats.R;

public class New_Password extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference table_user;

    String phoneNumber,fullName,ToDO;

    TextInputLayout newPass, rePass;

    Button btnContinue;

    final String ONE_DIGIT = "^(?=.*[0-9]).{6,}$";
    final String ONE_LOWER_CASE = "^(?=.*[a-z]).{6,}$";
    final String ONE_UPPER_CASE = "^(?=.*[A-Z]).{6,}$";
    final String ONE_SPECIAL_CHAR = "^(?=.*[@#$%^&+=]).{6,}$";
    final String NO_SPACE = "^(?=\\S+$).{6,}$";
    final String MIN_CHAR = "^[a-zA-Z0-9._-].{5,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);


        phoneNumber = getIntent().getStringExtra("phoneNo");
        fullName = getIntent().getStringExtra("name");
        ToDO = getIntent().getStringExtra("ToDO");

        newPass = findViewById(R.id.new_pass);
        rePass = findViewById(R.id.re_pass);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validatePassword() && !validatePassword2()) {
                    return;
                }

                String newPassword = newPass.getEditText().getText().toString().trim();
                String phoneNumber = getIntent().getStringExtra("phoneNo");
                ToDO = getIntent().getStringExtra("ToDO");

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
                reference.child(phoneNumber).child("password").setValue(newPassword);

                addNewUser();

                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();

            }
        });

    }

    private void addNewUser() {

        User addNew = new User(fullName,newPass.getEditText().getText().toString(),phoneNumber);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("user");

        table_user.child(phoneNumber).setValue(addNew);
    }


    private boolean validatePassword2() {
         String val = rePass.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            newPass.setError("Mật khẩu không được để trống!");
            return false;
        } else if (!val.matches(MIN_CHAR)) {
            newPass.setError("Mật khẩu phải có ít nhất 6 kí tự!");
            return false;
        } else if (!val.matches(ONE_DIGIT)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 chữ số!");
            return false;
        } else if (!val.matches(ONE_LOWER_CASE)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 chữ thường!");
            return false;
        } else if (!val.matches(ONE_UPPER_CASE)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 chữ viết hoa!");
            return false;
        } else if (!val.matches(ONE_SPECIAL_CHAR)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 kí tự đặc biệt!");
            return false;
        } else if (!val.matches(NO_SPACE)) {
            newPass.setError("Mật khẩu không được để khoảng cách!");
            return false;
        } else {
            newPass.setError(null);
            newPass.setErrorEnabled(false);
            return true;
        }

    }

    private boolean validatePassword() {
        String val = newPass.getEditText().getText().toString().trim();


        if (val.isEmpty()) {
            newPass.setError("Mật khẩu không được để trống!");
            return false;
        } else if (!val.matches(MIN_CHAR)) {
            newPass.setError("Mật khẩu phải có ít nhất 6 kí tự!");
            return false;
        } else if (!val.matches(ONE_DIGIT)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 chữ số!");
            return false;
        } else if (!val.matches(ONE_LOWER_CASE)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 chữ thường!");
            return false;
        } else if (!val.matches(ONE_UPPER_CASE)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 chữ viết hoa!");
            return false;
        } else if (!val.matches(ONE_SPECIAL_CHAR)) {
            newPass.setError("Mật khẩu phải có ít nhất 1 kí tự đặc biệt!");
            return false;
        } else if (!val.matches(NO_SPACE)) {
            newPass.setError("Mật khẩu không được để khoảng cách!");
            return false;
        } else {
            newPass.setError(null);
            newPass.setErrorEnabled(false);
            return true;
        }
    }
}