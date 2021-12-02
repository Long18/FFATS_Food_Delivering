package server.william.ffats;

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
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import server.william.ffats.Common.Common;
import server.william.ffats.Common.DirectionJSONParser;
import server.william.ffats.Database.SessionManager;
import server.william.ffats.Maps.Dijkstra;
import server.william.ffats.Maps.GraphConstructor;
import server.william.ffats.Maps.MapFunction;
import server.william.ffats.Maps.Node;
import server.william.ffats.Maps.OrderGraphItem;
import server.william.ffats.Model.Request;
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
    private static final int LOCATION_REQUEST = 7777;
    private final static int LOCATION_PERMISSION_REQUEST = 7001;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    //private LocationRequest mLocationRequest;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    double latitude;
    double longitude;
    private static LatLng currentLocation, guestLocation, mLocal;
    Marker mCurrentMarker;
    private Polyline mPolyline;
    private LatLngBounds latlngBounds;


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

        insertData();
        viewConstructor();

    }

    private void viewConstructor() {
        fab = findViewById(R.id.fab_map);

        if (ActivityCompat.checkSelfPermission(TrackingOrder.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(TrackingOrder.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST);
            createLocationRequest();
            buildGoogleApiClient();
        } else {
            if (checkPlayServices()) {
                createLocationRequest();
                createLocationCallBack();
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            }
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getlocation();
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f), 2000, null);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocal));

            }

        });

        //binding = ActivityTrackingOrderBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());

        mService = Common.getGeoCodeService();

        //mLastLocation = LocationServices.getFusedLocationProviderClient(TrackingOrder.this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void insertData() {
        isFirstTime = true;
        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

    }

    private void builLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();

                Toast.makeText(TrackingOrder.this, LastLocation.getLatitude() + "," + LastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                if (LastLocation != null) {
                    latitude = LastLocation.getLatitude();
                    longitude = LastLocation.getLongitude();

                    //Marker your location and move
                    LatLng yourLocation = new LatLng(latitude, longitude);
                    currentLocation = yourLocation;
                    if (isFirstTime) {
                        mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                        isFirstTime = false;
                    }
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));


                    if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);

                    //Add Marker for Order and draw route
                    drawRoute(getLocationFromAddress(TrackingOrder.this, Common.currentRequest.getAddress()));


                } else {
                    Toast.makeText(TrackingOrder.this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
                }
            }

        };
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

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
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private void drawRoutee(LatLng yourLocation, Request request) {
        if (mPolyline != null) {
            mPolyline.remove();
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            mService.getGeoCode(request.getAddress()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String lat = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();

                        String lng = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();

                        LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.package_filled);
                        bitmap = Common.scaleBitmap(bitmap, 70, 70);

                        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title("Order of " + userInformation.get(SessionManager.KEY_FULLNAME))
                                .position(orderLocation);
                        mMap.addMarker(marker);

                        guestLocation = orderLocation;

                        //draw route
                        mService.getDirections(yourLocation.latitude + "," + yourLocation.longitude,
                                orderLocation.latitude + "," + orderLocation.longitude)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        try {
                                            new ParserTask().execute(response.body().toString());
                                        } catch (Exception e) {
                                        } finally {
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        } else {
            if (request.getLatLng() != null && !request.getLatLng().isEmpty()) {
                String[] latLng = request.getLatLng().split(",");
                LatLng orderLocation = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.package_filled);
                bitmap = Common.scaleBitmap(bitmap, 70, 70);

                MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Order of " + userInformation.get(SessionManager.KEY_FULLNAME))
                        .position(orderLocation);
                mMap.addMarker(marker);

                mService.getDirections(mLastLocation.getLatitude() + "," + mLastLocation.getLongitude(),
                        orderLocation.latitude + "," + orderLocation.longitude)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                new ParserTask().execute(response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });
            }
        }

    }

    private void drawRoute( LatLng guestLatlon) {

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


        LatLng orderLocation = new LatLng(guestLatlon.latitude, guestLatlon.longitude);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.package_filled);
        bitmap = Common.scaleBitmap(bitmap, 70, 70);

        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title("Order of " + userInformation.get(SessionManager.KEY_FULLNAME))
                .position(orderLocation);
        mMap.addMarker(marker);

        guestLocation = orderLocation;
        ///////////////////////////////////////////////

        //orderLocation là vị trí nơi khách hàng

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                Node closestNodeForMyLocation = GraphConstructor
                        .findClosestNode(SessionManager.MAP_VALUE.getVertices(), location.getLatitude(),location.getLongitude());
                Node closestNodeForOrder = GraphConstructor
                        .findClosestNode(SessionManager.MAP_VALUE.getVertices(), orderLocation.latitude,orderLocation.longitude);

                Dijkstra dijkstra = new Dijkstra(SessionManager.MAP_VALUE.getMAX_length(), SessionManager.MAP_VALUE.getGraph());
                int temp =SessionManager.MAP_VALUE.getVertices().indexOf(closestNodeForMyLocation);
                dijkstra.runDijkstraWithPriorityQueue(temp);
                OrderGraphItem thisOrderPath = new OrderGraphItem();
                dijkstra.getWayForDijkstraWithPriorityQueueToGraph(SessionManager.MAP_VALUE.getVertices(),
                        thisOrderPath,
                        SessionManager.MAP_VALUE.getVertices().indexOf(closestNodeForOrder));

                MapFunction.DrawVertexAndWay(mMap,thisOrderPath.getWayList(),closestNodeForMyLocation,closestNodeForOrder);
            }
        });


        ///////////////////////////////////////////////

        //draw route
        /*mService.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                orderLocation.latitude+","+orderLocation.longitude)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try{
                            new ParserTask().execute(response.body().toString());
                        }
                        catch(Exception e){}finally {}

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });*/


    }

    private void createLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();

                mLocal = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

                LatLng yourLocation = new LatLng(latitude, longitude);

                if (mCurrentMarker != null){
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

                    drawRoute(getLocationFromAddress(TrackingOrder.this, Common.currentRequest.getAddress()));

                    //drawRoutee(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),Common.currentRequest);
                }
            }
        };

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
                    } else {
                        Toast.makeText(TrackingOrder.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        builLocationCallBack();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

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
//        mGoogleApiClient.disconnect();
        //stopLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    private class ParserTask extends AsyncTask<String, Integer,
            List<List<HashMap<String, String>>>> {

        ProgressDialog mDialog = new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {

            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes = parser.parse(jsonObject);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;

        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < lists.size(); i++) {

                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
            }

            mMap.addPolyline(lineOptions);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(TrackingOrder.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(TrackingOrder.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST);

            createLocationRequest();
            buildGoogleApiClient();

        } else {
            if (checkPlayServices()) {

                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastLocation = location;

                        LatLng yourLocation = new LatLng(location.getLatitude(),location.getLongitude());

                        mCurrentMarker = mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f), 2000, null);

                    }
                });
            }
        }
        //Add Marker for Order and draw route
        drawRoute(getLocationFromAddress(TrackingOrder.this, Common.currentRequest.getAddress()));


        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {

                createLocationRequest();
                buildGoogleApiClient();
                builLocationCallBack();


            }
        }*/
    }

}