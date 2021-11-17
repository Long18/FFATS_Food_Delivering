package server.william.ffats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import server.william.ffats.Common.Common;
import server.william.ffats.Common.DirectionJSONParser;
import server.william.ffats.Database.SessionManager;
import server.william.ffats.Remote.IGeoCoordinates;
import server.william.ffats.databinding.ActivityTrackingOrderBinding;


public class TrackingOrder extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    FloatingActionButton fab;

    private GoogleMap mMap;
    private ActivityTrackingOrderBinding binding;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 7000;
    private final static int LOCATION_PERMISSION_REQUEST = 7001;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    //private LocationRequest mLocationRequest;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    double latitude;
    double longitude;

    boolean isFirstTime = false;

    private static int UPDATE_INTERVAL = 5000;
    private static int FAST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private IGeoCoordinates mService;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        isFirstTime = true;

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        fab = findViewById(R.id.fab_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getlocation();
                builLocationCallBack();
            }

        });

//        binding = ActivityTrackingOrderBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        mService = Common.getGeoCodeService();

        //mLastLocation = LocationServices.getFusedLocationProviderClient(TrackingOrder.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {

                createLocationRequest();
                buildGoogleApiClient();
                builLocationCallBack();

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    private void builLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();

                Toast.makeText(TrackingOrder.this, LastLocation.getLatitude() + "," +LastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                if (LastLocation != null) {
                    latitude = LastLocation.getLatitude();
                    longitude = LastLocation.getLongitude();

                    //Marker your location and move
                    LatLng yourLocation = new LatLng(latitude, longitude);
                    if (isFirstTime){
                        mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                        isFirstTime = false;
                    }


                    if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);

                    //Add Marker for Order and draw route
                    drawRoute(yourLocation,getLocationFromAddress(TrackingOrder.this,Common.currentRequest.getAddress()));


                } else {
                    Toast.makeText(TrackingOrder.this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
                }
            }

        };
    }

    /*private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestRuntimePermission();
        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // Got last known location. In some rare situations this can be null.
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                //Marker your location and move
                LatLng yourLocation = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                //Add Marker for Order and draw route
                drawRoute(yourLocation, Common.currentRequest.getAddress());
            } else {
                Toast.makeText(TrackingOrder.this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private void drawRoute(LatLng yourLocation, LatLng latlon) {
//        mService.getGeoCode(address).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                try {
//                    JSONObject jsonObject = new JSONObject(response.body().toString());
//
//                    String lat = ((JSONArray) jsonObject.get("results"))
//                            .getJSONObject(0)
//                            .getJSONObject("geometry")
//                            .getJSONObject("location")
//                            .get("lat").toString();
//
//                    String lng = ((JSONArray) jsonObject.get("results"))
//                            .getJSONObject(0)
//                            .getJSONObject("geometry")
//                            .getJSONObject("location")
//                            .get("lng").toString();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//
//            }
//        });

        LatLng orderLocation = new LatLng(latlon.latitude,latlon.longitude);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.package_filled);
        bitmap = Common.scaleBitmap(bitmap, 70, 70);

        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title("Order of " + userInformation.get(SessionManager.KEY_FULLNAME))
                .position(orderLocation);
        mMap.addMarker(marker);

        //draw route
        mService.getDirections(yourLocation.latitude + "," + yourLocation.longitude,
                orderLocation.latitude + "," + orderLocation.longitude)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        new TrackingOrder.ParserTask().execute(response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(TrackingOrder.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }

//                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                        if( mLastLocation == null ){
//                            LocationServices.FusedLocationApi
//                                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
//                                @Override
//                                public void onLocationChanged(Location location) {
//                                    mLastLocation = location;
//                                    displayLocation();
//                                }
//                            });
//                        }
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
                                connectionResult.startResolutionForResult(TrackingOrder.this, PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(TrackingOrder.this, "bug", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
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

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();

                        //displayLocation();
                        builLocationCallBack();

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
                    }else {
                        Toast.makeText(TrackingOrder.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {mMap = googleMap;}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        builLocationCallBack();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ){

            return;
        }
        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient,locationRequest,this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        //displayLocation();
        builLocationCallBack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        stopLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog = new ProgressDialog(TrackingOrder.this);
        private Polyline _currentPolyline;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;
            try {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes = parser.parse(jObject);

            }catch (JSONException e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();


            hideRoute();

            ArrayList<LatLng> points = null;
            PolylineOptions polylineOptions =null;

            for (int i = 0; i < lists.size(); i++){
                points = new ArrayList<LatLng>();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j ++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            //_currentPolyline = mMap.addPolyline(polylineOptions);
        }

        public void hideRoute()
        {
            if (_currentPolyline != null)
            {
                _currentPolyline.remove();
            }
        }
    }



}