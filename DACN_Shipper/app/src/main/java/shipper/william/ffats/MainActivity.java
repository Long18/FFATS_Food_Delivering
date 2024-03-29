package shipper.william.ffats;

import static com.google.android.gms.location.LocationServices.API;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import shipper.william.ffats.Account.Login;
import shipper.william.ffats.Common.Common;
import shipper.william.ffats.Database.SessionManager;
import shipper.william.ffats.Remote.LocationResolver;
import shipper.william.ffats.Service.ListenOrder;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //region Declare
    Button btnSignIn, btnSignUp;

    FirebaseDatabase db;
    DatabaseReference table_shippers;

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
                startActivity(new Intent(MainActivity.this, Login.class));
            }
            //endregion
            //region btnSignUp
            if (v.getId() == R.id.btnSignUp) {


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
    }

    private void insertData() {
        db = FirebaseDatabase.getInstance();
        table_shippers = db.getReference("Shippers");

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


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDialogLocation();
            return;
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            showDialogCall();
            return;
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
        rmbCodeCountry = Paper.book().read("+84");

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

    public void getRecentLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();

                if (LastLocation != null) {

                    try {
                        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        addresses = geocoder.getFromLocation(LastLocation.getLatitude(), LastLocation.getLongitude(), 1);

                        location = addresses.get(0).getAddressLine(1);

                        sessionManager.createLocation(addresses.get(0).getAdminArea());

                        fistTime = getSharedPreferences("tutorialScreen", MODE_PRIVATE);
                        boolean isFirstTime = fistTime.getBoolean("firstTimeGetLocation", true);

                        if (isFirstTime) {
                            SharedPreferences.Editor editor = fistTime.edit();
                            editor.putBoolean("firstTimeGetLocation", false);
                            editor.commit();

                            sessionManager.createAddress(addresses.get(0).getAddressLine(0));
                        } else {
                            if (userInformation.get(SessionManager.KEY_ADDRESS) == "Việt Nam") {
                                SharedPreferences.Editor editor = fistTime.edit();
                                editor.putBoolean("firstTimeGetLocation", true);
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

    public void showDialogCall() {
        final Dialog allowPermission = new Dialog(MainActivity.this, R.style.df_dialog);
        allowPermission.setContentView(R.layout.dialog_call);

        allowPermission.findViewById(R.id.btnAllowAccess).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allowPermission != null && allowPermission.isShowing()) {
                    allowPermission.dismiss();
                }
                mLocationResolver = new LocationResolver(MainActivity.this);
                mLocationResolver.isCallPermissionEnabled();
                mLocationResolver.checkPermissionCall(MainActivity.this);
            }
        });
        allowPermission.show();
    }

    public void showDialogLocation() {
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

    private void login(String phone, String pwd, String cpp) {
        if (Common.isConnectedToInternet(getBaseContext())) {

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

            final String phoneNo = "+84" + getUserPhoneNumber;

            //Database
            Query checkUser = FirebaseDatabase.getInstance().getReference("Shippers").orderByChild("phone").equalTo(phoneNo);

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
                            String mImage = snapshot.child(phoneNo).child("image").getValue(String.class);
                            String mSumOrders = snapshot.child(phoneNo).child("sumOrders").getValue(String.class);

                            //Create Database Store
                            SessionManager sessionManager = new SessionManager(MainActivity.this, SessionManager.SESSION_USER);
                            sessionManager.createLoginSession(mName, mPhoneNo, mPassword, mImage, mSumOrders);

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

        } else {
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