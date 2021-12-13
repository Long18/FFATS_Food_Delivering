package shipper.william.ffats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shipper.william.ffats.Common.Common;
import shipper.william.ffats.Database.SessionManager;
import shipper.william.ffats.Maps.Dijkstra;
import shipper.william.ffats.Maps.GraphConstructor;
import shipper.william.ffats.Maps.MapFunction;
import shipper.william.ffats.Maps.Node;
import shipper.william.ffats.Maps.OrderGraphItem;
import shipper.william.ffats.databinding.ActivityTrackingOrderBinding;

public class TrackingOrder extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //region Declare
    ImageButton btnMarker, btnCall, btnDone;
    TextView txtID, txtName, txtAddress;

    private GoogleMap mMap;

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
    private static LatLng currentLocation, guestLocation, mLocal;
    Marker mCurrentMarker;
    private Polyline mPolyline;
    private LatLngBounds latlngBounds;


    boolean isFirstTime = false;

    private static int UPDATE_INTERVAL = 5000;
    private static int FAST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;


    SessionManager sessionManager;
    HashMap<String, String> userInformation;
    ArrayList<Polyline> currentPolyLines;
    //endregion

    //region Function Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        insertData();
        viewConstructor();

    }

    private void viewConstructor() {
        btnMarker = findViewById(R.id.fab_marker);
        btnCall = findViewById(R.id.maps_btnCalling);
        btnDone = findViewById(R.id.maps_btnDone);
        txtID = findViewById(R.id.maps_txtID);
        txtName = findViewById(R.id.maps_txtName);
        txtAddress = findViewById(R.id.maps_txtAddress);

        txtID.setText(Common.currentRequest.getTotal());
        txtName.setText(Common.currentRequest.getName());
        txtAddress.setText(Common.currentRequest.getAddress());

        if (ActivityCompat.checkSelfPermission(TrackingOrder.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(TrackingOrder.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST);
            createLocationRequest();
            buildGoogleApiClient();
        } else {
            if (checkPlayServices()) {
                createLocationRequest();
                createLocationCallBack();
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            }
        }


        btnMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f), 2000, null);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocal));

            }

        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Common.currentRequest.getPhone()));
                if (ActivityCompat.checkSelfPermission(TrackingOrder.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(TrackingOrder.this,
                                android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TrackingOrder.this, new String[]{
                            Manifest.permission.CALL_PHONE
                    }, LOCATION_REQUEST);
                    return;
                }
                startActivity(intent);
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeOrder();
            }
        });

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
    //endregion

    //region Function
    private void completeOrder() {
        FirebaseDatabase.getInstance().getReference("PendingOrders")
                .child(userInformation.get(SessionManager.KEY_PHONENUMBER))
                .child(Common.KEY_REALTIME)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String, Object> updateStatus = new HashMap<>();
                        updateStatus.put("status", "03");

                        FirebaseDatabase.getInstance().getReference("Requests")
                                .child(Common.KEY_REALTIME)
                                .updateChildren(updateStatus)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        FirebaseDatabase.getInstance().getReference("PendingOrders")
                                                .child(Common.KEY_REALTIME)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        finish();
                                                    }
                                                });
                                        FirebaseDatabase.getInstance().getReference("LocationRealTime")
                                                .child(Common.KEY_REALTIME)
                                                .removeValue()
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("Bug", e.getMessage());
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    private void builLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location mLastLocation = locationResult.getLastLocation();

                Toast.makeText(TrackingOrder.this, mLastLocation.getLatitude() + "," + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();


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


                    if (ActivityCompat.checkSelfPermission(TrackingOrder.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackingOrder.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void drawRoute(LatLng guestLatlon) {


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

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                Node closestNodeForMyLocation = GraphConstructor
                        .findClosestNode(SessionManager.MAP_VALUE.getVertices(), location.getLatitude(), location.getLongitude());
                Node closestNodeForOrder = GraphConstructor
                        .findClosestNode(SessionManager.MAP_VALUE.getVertices(), orderLocation.latitude, orderLocation.longitude);

                Dijkstra dijkstra = new Dijkstra(SessionManager.MAP_VALUE.getMAX_length(), SessionManager.MAP_VALUE.getGraph());
                int temp = SessionManager.MAP_VALUE.getVertices().indexOf(closestNodeForMyLocation);
                dijkstra.runDijkstraWithPriorityQueue(temp);
                OrderGraphItem thisOrderPath = new OrderGraphItem();
                dijkstra.getWayForDijkstraWithPriorityQueueToGraph(SessionManager.MAP_VALUE.getVertices(),
                        thisOrderPath,
                        SessionManager.MAP_VALUE.getVertices().indexOf(closestNodeForOrder));
                removeCurrentPolylines();
                currentPolyLines = MapFunction.DrawVertexAndWay(mMap, thisOrderPath.getWayList(), closestNodeForMyLocation, closestNodeForOrder);

                //Update location of shipper to Firebase
                Common.updateLocationRealTime(Common.KEY_REALTIME, mLastLocation);

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

                LatLng yourLocation = new LatLng(latitude, longitude);

                if (mCurrentMarker != null) {
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

                    drawRoute(getLocationFromAddress(TrackingOrder.this, Common.currentRequest.getAddress()));

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
                        if (ActivityCompat.checkSelfPermission(TrackingOrder.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(TrackingOrder.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
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
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        boolean isSuccess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        if (!isSuccess) {
            Log.d("BUG", "lỗi map rồi má ơi");
        }

        if (ActivityCompat.checkSelfPermission(TrackingOrder.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(TrackingOrder.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
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

                        LatLng yourLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        mCurrentMarker = mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f), 2000, null);

                    }
                });
            }
        }
        //Add Marker for Order and draw route
        drawRoute(getLocationFromAddress(TrackingOrder.this, Common.currentRequest.getAddress()));

    }

    //endregion
}