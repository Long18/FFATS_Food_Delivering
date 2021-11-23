package server.william.ffats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import server.william.ffats.Remote.LocationResolver;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;
    TextView txtSlogan;

    LocationResolver mLocationResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialog();
        }

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        txtSlogan = findViewById(R.id.txtSlogan);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignIn.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }
        });
    }

    public void showDialog() {
        final Dialog allowPermission = new Dialog(MainActivity.this, R.style.df_dialog);
        allowPermission.setContentView(R.layout.dialog_location);

        allowPermission.findViewById(R.id.btnAllowAccess).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allowPermission != null && allowPermission.isShowing()) {
                    allowPermission.dismiss();
                }
                mLocationResolver = new LocationResolver(MainActivity.this);
                mLocationResolver.isLocationPermissionEnabled();
                mLocationResolver.checkPermissionLocation(MainActivity.this);
            }
        });
        allowPermission.show();
    }
}