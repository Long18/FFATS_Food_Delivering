package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Maps.Dijkstra;
import client.william.ffats.Maps.GraphConstructor;
import client.william.ffats.Maps.MapFunction;
import client.william.ffats.Maps.Node;
import client.william.ffats.Maps.OrderGraphItem;
import client.william.ffats.Model.LocationShipper;
import client.william.ffats.Model.Request;
import client.william.ffats.databinding.ActivityTrackingOrderBinding;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private ActivityTrackingOrderBinding binding;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 7000;
    private static final int LOCATION_REQUEST = 7777;
    private final static int LOCATION_PERMISSION_REQUEST = 7001;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    double latitude;
    double longitude;
    private static LatLng currentLocation, mshipperLocation, mLocal;
    Marker mCurrentMarker;
    private Polyline mPolyline;
    private LatLngBounds latlngBounds;

    boolean isFirstTime = false;

    private static int UPDATE_INTERVAL = 5000;
    private static int FAST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    Request currentOrders;

    FirebaseDatabase database;
    DatabaseReference requests, locationRealTime;
    ArrayList<Polyline> currentPolyLines;

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        insertData();
        viewConstructor();

    }

    private void viewConstructor() {
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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void insertData() {
        isFirstTime = true;
        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Request");
        locationRealTime = database.getReference("LocationRealTime");

    }
    //endregion

    //region Function
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

                    locationRealTime.child(Common.KEY_REALTIME)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    LocationShipper locationShipper = snapshot.getValue(LocationShipper.class);

                                    LatLng shipperLocation = new LatLng(locationShipper.getLat(),locationShipper.getLng());

                                    //Add Marker for Order and draw route
                                    drawRoute(shipperLocation);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


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

    private void drawRoute(LatLng shipperLocationn) {
        LatLng shipperLocation = new LatLng(shipperLocationn.latitude, shipperLocationn.longitude);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shipper_ic);
        bitmap = Common.scaleBitmap(bitmap, 70, 70);

        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title("Order of " + userInformation.get(SessionManager.KEY_FULLNAME))
                .position(shipperLocation);
        mMap.addMarker(marker);

        mshipperLocation = shipperLocation;
        ///////////////////////////////////////////////

        //mshipperLocation là vị trí nơi shipper

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                Node closestNodeForMyLocation = GraphConstructor
                        .findClosestNode(SessionManager.MAP_VALUE.getVertices(), location.getLatitude(), location.getLongitude());
                Node closestNodeForOrder = GraphConstructor
                        .findClosestNode(SessionManager.MAP_VALUE.getVertices(), shipperLocation.latitude, shipperLocation.longitude);

                Dijkstra dijkstra = new Dijkstra(SessionManager.MAP_VALUE.getMAX_length(), SessionManager.MAP_VALUE.getGraph());
                int temp = SessionManager.MAP_VALUE.getVertices().indexOf(closestNodeForMyLocation);
                dijkstra.runDijkstraWithPriorityQueue(temp);
                OrderGraphItem thisOrderPath = new OrderGraphItem();
                dijkstra.getWayForDijkstraWithPriorityQueueToGraph(SessionManager.MAP_VALUE.getVertices(),
                        thisOrderPath,
                        SessionManager.MAP_VALUE.getVertices().indexOf(closestNodeForOrder));
                removeCurrentPolylines();
                MapFunction.DrawVertexAndWay(mMap, thisOrderPath.getWayList(), closestNodeForMyLocation, closestNodeForOrder);
            }
        });

    }

    private void removeCurrentPolylines() {
        if (currentPolyLines != null) {
            for (Polyline item :
                    currentPolyLines) {
                item.remove();
            }
        }
    }

    private void createLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();

                mLocal = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                if (mCurrentMarker != null) {
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

                    locationRealTime.child(Common.KEY_REALTIME)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    LocationShipper locationShipper = snapshot.getValue(LocationShipper.class);

                                    LatLng shipperLocation = new LatLng(locationShipper.getLat(),locationShipper.getLng());

                                    //Add Marker for Order and draw route
                                    drawRoute(shipperLocation);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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

    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

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
                        locationRealTime.child(Common.KEY_REALTIME)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        LocationShipper locationShipper = snapshot.getValue(LocationShipper.class);

                                        LatLng shipperLocation = new LatLng(locationShipper.getLat(),locationShipper.getLng());

                                        //Add Marker for Order and draw route
                                        drawRoute(shipperLocation);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mshipperLocation));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f), 2000, null);

                                        trackingLocation();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }
                });
            }
        }


    }

    private void trackingLocation() {
        requests.child(Common.KEY_REALTIME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentOrders = snapshot.getValue(Request.class);

                        locationRealTime.child(Common.KEY_REALTIME)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        LocationShipper locationShipper = snapshot.getValue(LocationShipper.class);

                                        LatLng shipperLocation = new LatLng(locationShipper.getLat(),locationShipper.getLng());

                                        //Add Marker for Order and draw route
                                        drawRoute(shipperLocation);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mshipperLocation));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f), 2000, null);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}