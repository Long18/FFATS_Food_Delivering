package client.william.ffats.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import client.william.ffats.Home;
import client.william.ffats.Model.User;
import client.william.ffats.R;

public class New_Password extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference table_user;

    String phoneNumber,fullName,ToDO;

    TextInputLayout txtInNewPassword, txtInRePassword;

    Button btnContinue;

    boolean newPassword_Check = false;
    boolean rePassword_Check = false;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region btn SignIn
            if (v.getId() == R.id.newPassword_btnContinue){

                String newPassword = txtInNewPassword.getEditText().getText().toString().trim();
                String phoneNumber = getIntent().getStringExtra("phoneNo");
                ToDO = getIntent().getStringExtra("ToDO");

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
                reference.child(phoneNumber).child("password").setValue(newPassword);

                addNewUser();

                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
            }
            //endregion
        }
    };

    //region Function Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        viewConstructor();
        importData();

    }

    private void viewConstructor() {
        txtInNewPassword = findViewById(R.id.new_pass);
        txtInRePassword = findViewById(R.id.re_pass);
        btnContinue = findViewById(R.id.newPassword_btnContinue);

        btnContinue.setOnClickListener(onClickListener);

        txtInNewPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // check
                String result = Validate.validatePassword(txtInNewPassword.getEditText().getText().toString());
                if (result != null){
                    txtInNewPassword.setError(result);
                    newPassword_Check = false;
                }else{
                    txtInNewPassword.setError(null);
                    newPassword_Check = true;
                }
                if (newPassword_Check && rePassword_Check){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });

        txtInRePassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!txtInRePassword.getEditText().getText().toString().equals(txtInNewPassword.getEditText().getText().toString())){
                    txtInRePassword.setError("Mật khẩu không khớp, vui lòng nhập lại mật khẩu.");
                    rePassword_Check = false;
                }
                else{
                    txtInRePassword.setError(null);
                    rePassword_Check = true;
                }
                if (newPassword_Check && rePassword_Check){
                    btnContinue.setEnabled(true);
                }else {
                    btnContinue.setEnabled(false);
                }
            }
        });
    }

    private void importData(){
        Bundle bundle = getIntent().getExtras();

        phoneNumber = bundle.getString("phoneNo");
        fullName = bundle.getString("name");
        ToDO = bundle.getString("ToDO");
    }
    //endregion

    //region Function
    private void addNewUser() {

        User addNew = new User(fullName, txtInNewPassword.getEditText().getText().toString(),phoneNumber);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("user");

        table_user.child(phoneNumber).setValue(addNew);
    }
    //endregion



}