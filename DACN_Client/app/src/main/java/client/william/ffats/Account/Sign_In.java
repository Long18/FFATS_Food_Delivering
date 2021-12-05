package client.william.ffats.Account;

import static com.google.android.gms.location.LocationServices.API;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.rey.material.widget.CheckBox;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Home;
import client.william.ffats.MainActivity;
import client.william.ffats.R;
import client.william.ffats.Remote.LocationResolver;
import io.paperdb.Paper;

public class Sign_In extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //region Declare Variable
    TextInputEditText editPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    TextView txtResetPassword,txtCreateAccount;

    CountryCodePicker countryNumber;

    public String location;
    Geocoder geocoder;
    List<Address> addresses;
    LocationCallback locationCallback;
    LocationResolver mLocationResolver;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int LOCATION_REQUEST = 7777;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 7000;
    private static final int LOCATION_PERMISSION_REQUEST = 7007;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLCAEMENT = 10;

    boolean phoneCheck = false;
    boolean passCheck = false;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Sign In Auto
            if (v.getId() == R.id.signIn_btnContinue){
                if(Common.isConnectedToInternet(getBaseContext())) {

                    //Remember User
                    if (ckbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,editPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                        Paper.book().write(Common.CCP_KEY,countryNumber.getFullNumber());
                    }


                    final ProgressDialog mDialog = new ProgressDialog(Sign_In.this);
                    mDialog.setMessage("Please wait...");
                    Drawable drawable = new ProgressBar(Sign_In.this).getIndeterminateDrawable().mutate();
                    drawable.setColorFilter(ContextCompat.getColor(Sign_In.this, R.color.colorPrimary),
                            PorterDuff.Mode.SRC_IN);
                    mDialog.setIndeterminateDrawable(drawable);
                    mDialog.show();

                    String getUserPhoneNumber = editPhone.getText().toString().trim();// Get Phone Num

                    if (getUserPhoneNumber.charAt(0) == '0') {
                        getUserPhoneNumber = getUserPhoneNumber.substring(1);
                    }

                    final String phoneNo = "+" + countryNumber.getFullNumber() + getUserPhoneNumber;

                    //Database
                    Query checkUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone").equalTo(phoneNo);

                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                editPhone.setError(null);

                                String systemPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                                if (systemPassword.equals(edtPassword.getText().toString())) {
                                    edtPassword.setError(null);

                                    startActivity(new Intent(Sign_In.this, Home.class));
                                    finish();

                                    String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                                    String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                                    String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);
                                    String mImage = snapshot.child(phoneNo).child("image").getValue(String.class);
                                    String mAddress = snapshot.child(phoneNo).child("address").getValue(String.class);
                                    String mEmail = snapshot.child(phoneNo).child("email").getValue(String.class);

                                    //Create Database Store
                                    SessionManager sessionManager = new SessionManager(Sign_In.this, SessionManager.SESSION_USER);
                                    sessionManager.createLoginSession(mName, mPhoneNo, mPassword,mImage,mAddress,mEmail);

                                    mDialog.dismiss();
                                    Toast.makeText(Sign_In.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                                } else {
                                    mDialog.dismiss();
                                    Toast.makeText(Sign_In.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Sign_In.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Sign_In.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(Sign_In.this, "Please check internet", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            //endregion
            //region Reset Password
            if (v.getId() == R.id.txtResetPassword){
                startActivity(new Intent(Sign_In.this, Reset_Password.class));
            }
            //endregion
            //region Sign In
            if (v.getId() == R.id.txtCreateAccount){
                startActivity(new Intent(Sign_In.this, Sign_Up.class));
            }
            //endregion

        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);

        viewConstructor();

    }

    private void viewConstructor(){

        Paper.init(this);

        getRecentLocation();

        editPhone = findViewById(R.id.txtEditTextPhone);
        edtPassword = findViewById(R.id.txtEditTextPassword);
        countryNumber = findViewById(R.id.countryNumber);
        btnSignIn = findViewById(R.id.signIn_btnContinue);
        ckbRemember = findViewById(R.id.ckbRemember);
        txtResetPassword = findViewById(R.id.txtResetPassword);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);


        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // check
                String result = Validate.validatePhoneNumber(editPhone.getText().toString());
                if (result != null){
                    editPhone.setError(result);
                    phoneCheck = false;
                }else{
                    editPhone.setError(null);
                    phoneCheck = true;
                }
                if (phoneCheck && passCheck){
                    btnSignIn.setEnabled(true);
                }else {
                    btnSignIn.setEnabled(false);
                }
            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // check
                String result = Validate.validatePassword(edtPassword.getText().toString());
                if (result != null){
                    edtPassword.setError(result);
                    passCheck = false;
                }else{
                    edtPassword.setError(null);
                    passCheck = true;
                }
                if (phoneCheck && passCheck){
                    btnSignIn.setEnabled(true);
                }else {
                    btnSignIn.setEnabled(false);
                }
            }
        });

        btnSignIn.setOnClickListener(onClickListener);
        txtResetPassword.setOnClickListener(onClickListener);
        txtCreateAccount.setOnClickListener(onClickListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            getRecentLocation();
        } else {
            if (checkPlayServices()) {
                createLocationRequest();
                buildGoogleApiClient();
                checkPermissionLocation();
                getRecentLocation();
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }

    }
    //endregion

    //region Function
    public void Register(View View) {
        startActivity(new Intent(Sign_In.this, Sign_Up.class));
    }

    public void getRecentLocation(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();

                if (LastLocation != null) {

                    try {
                        geocoder = new Geocoder(Sign_In.this, Locale.getDefault());
                        addresses = geocoder.getFromLocation(LastLocation.getLatitude(),LastLocation.getLongitude(),1);


                        double latitude = LastLocation.getLatitude();
                        double longitude = LastLocation.getLongitude();

                        location = addresses.get(0).getAddressLine(1);
                        SessionManager sessionManager = new SessionManager(Sign_In.this, SessionManager.SESSION_USER);
                        sessionManager.createLocation(addresses.get(0).getAdminArea());
                        sessionManager.createAddress(addresses.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                }
            }

        };
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLCAEMENT);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(Sign_In.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(Sign_In.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(Sign_In.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }

                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (connectionResult.hasResolution()) {
                            try {
                                // Start an Activity that tries to resolve the error
                                connectionResult.startResolutionForResult(Sign_In.this, PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(Sign_In.this, "bug", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addApi(API)
                .build();

        mGoogleApiClient.connect();
    }

    private void checkPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(Sign_In.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Sign_In.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST);
        } else {
            if (checkPlayServices()) {

                createLocationRequest();
                buildGoogleApiClient();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //endregion

}