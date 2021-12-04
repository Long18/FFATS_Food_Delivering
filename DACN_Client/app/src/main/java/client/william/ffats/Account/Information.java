package client.william.ffats.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;

import client.william.ffats.Database.SessionManager;
import client.william.ffats.R;
import io.paperdb.Paper;

public class Information extends AppCompatActivity {

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    TextView txtName,txtAdress;
    TextInputLayout tilName,tilEmail,tilNumberPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        insertData();
        viewConstructor();
    }

    private void insertData() {
        Paper.init(this);
    }

    private void viewConstructor() {
        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        txtName = findViewById(R.id.txtFullName);
        txtAdress = findViewById(R.id.txtAddress);
        tilName = findViewById(R.id.txtName);
        tilEmail = findViewById(R.id.txtEmail);
        tilNumberPhone = findViewById(R.id.txtPhoneNumner);

        txtName.setText(userInformation.get(SessionManager.KEY_FULLNAME));
        txtAdress.setText("Address: " + userInformation.get(SessionManager.KEY_ADDRESS));

        tilName.getEditText().setText(userInformation.get(SessionManager.KEY_FULLNAME));
        tilEmail.getEditText().setText(userInformation.get(SessionManager.KEY_EMAIL));
        tilNumberPhone.getEditText().setText(userInformation.get(SessionManager.KEY_PHONENUMBER));
    }
}