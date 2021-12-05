package client.william.ffats;

import static com.google.android.gms.location.LocationServices.API;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import client.william.ffats.Account.Sign_In;
import client.william.ffats.Account.Sign_Up;
import client.william.ffats.Common.Common;
import client.william.ffats.Common.Tutorial;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Remote.LocationResolver;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //region Declare Variable
    Button btnSignIn, btnSignUp;

    FirebaseDatabase db;
    DatabaseReference table_user;

    String rmbNumberPhone, rmbPassword, rmbCodeCountry;

    public String location;
    Geocoder geocoder;
    List<Address> addresses;
    LocationCallback locationCallback;
    LocationResolver mLocationResolver;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    SharedPreferences fistTime;

    private static final int LOCATION_REQUEST = 7777;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 7000;
    private static final int LOCATION_PERMISSION_REQUEST = 7007;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLCAEMENT = 10;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region btnSignIn
            if (v.getId() == R.id.btnSignIn) {
                startActivity(new Intent(MainActivity.this, Sign_In.class));
            }
            //endregion
            //region btnSignUp
            if (v.getId() == R.id.btnSignUp) {
                startActivity(new Intent(MainActivity.this, Sign_Up.class));
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
        setContentView(R.layout.activity_main);

        insertData();
        viewConstructor();
        printKeyHash(MainActivity.this);

    }

    private void insertData() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

        sessionManager = new SessionManager(MainActivity.this, SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();
        db = FirebaseDatabase.getInstance();
        table_user = db.getReference("user");

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialog();
        }
    }

    private void viewConstructor() {
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        Paper.init(this);

        btnSignIn.setOnClickListener(onClickListener);
        btnSignUp.setOnClickListener(onClickListener);

        //Auto login when already click remember button
        rmbNumberPhone = Paper.book().read(Common.USER_KEY);
        rmbPassword = Paper.book().read(Common.PWD_KEY);
        rmbCodeCountry = Paper.book().read(Common.CCP_KEY);

        FacebookSdk.sdkInitialize(getApplicationContext());

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        if (sessionManager.checkUserLogin()) {
            if (rmbNumberPhone != null && rmbPassword != null) {
                if (!rmbNumberPhone.isEmpty() && !rmbPassword.isEmpty()) {
                    login(rmbNumberPhone, rmbPassword, rmbCodeCountry);
                }
            }
        }

    }
    //endregion

    //region Function
    public void getRecentLocation(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();

                if (LastLocation != null) {

                    try {
                        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        addresses = geocoder.getFromLocation(LastLocation.getLatitude(),LastLocation.getLongitude(),1);

                        location = addresses.get(0).getAddressLine(1);

                        sessionManager.createLocation(addresses.get(0).getAdminArea());

                        fistTime = getSharedPreferences("tutorialScreen",MODE_PRIVATE);
                        boolean isFirstTime = fistTime.getBoolean("firstTimeGetLocation",true);

                        if (isFirstTime){
                            SharedPreferences.Editor editor = fistTime.edit();
                            editor.putBoolean("firstTimeGetLocation",false);
                            editor.commit();

                            sessionManager.createAddress(addresses.get(0).getAddressLine(0));
                        }
                        else {
                            if (userInformation.get(SessionManager.KEY_ADDRESS) == "Việt Nam"){
                                SharedPreferences.Editor editor = fistTime.edit();
                                editor.putBoolean("firstTimeGetLocation",true);
                                editor.commit();
                                return;
                            }
                            //get address from firebase

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                }
            }

        };
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

    //region Get KeyHash
    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (android.content.pm.Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }
    //endregion
    private void login(String phone, String pwd,String cpp) {
        if(Common.isConnectedToInternet(getBaseContext())) {

            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please wait...");
            Drawable drawable = new ProgressBar(MainActivity.this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN);
            mDialog.setIndeterminateDrawable(drawable);
            mDialog.show();

            String getUserPhoneNumber = phone;// Get Phone Num

            if (getUserPhoneNumber.charAt(0) == '0') {
                getUserPhoneNumber = getUserPhoneNumber.substring(1);
            }

            final String phoneNo = "+" + cpp + getUserPhoneNumber;

            //Database
            Query checkUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone").equalTo(phoneNo);

            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {


                        String systemPassword = snapshot.child(phoneNo).child("password").getValue(String.class);

                        if (systemPassword.equals(pwd)) {


                            startActivity(new Intent(MainActivity.this, Home.class));
                            finish();

                            String mName = snapshot.child(phoneNo).child("name").getValue(String.class);
                            String mPhoneNo = snapshot.child(phoneNo).child("phone").getValue(String.class);
                            String mPassword = snapshot.child(phoneNo).child("password").getValue(String.class);
                            String mImage = snapshot.child(phoneNo).child("password").getValue(String.class);

                            //Create Database Store
                            SessionManager sessionManager = new SessionManager(MainActivity.this, SessionManager.SESSION_USER);
                            sessionManager.createLoginSession(mName, mPhoneNo, mPassword,mImage);

                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


                        } else {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Toast.makeText(MainActivity.this, "Please check internet", Toast.LENGTH_SHORT).show();
            return;
        }
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
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
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
                                connectionResult.startResolutionForResult(MainActivity.this, PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "bug", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addApi(API)
                .build();

        mGoogleApiClient.connect();
    }

    private void checkPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
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