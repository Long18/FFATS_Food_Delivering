package shipper.william.ffats;

import static com.google.android.gms.location.LocationServices.API;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import shipper.william.ffats.Common.Common;
import shipper.william.ffats.Database.SessionManager;
import shipper.william.ffats.Maps.GraphConstructor;
import shipper.william.ffats.Maps.MapValue;
import shipper.william.ffats.Maps.XML_reading;
import shipper.william.ffats.Model.Request;
import shipper.william.ffats.Remote.LocationResolver;
import shipper.william.ffats.Service.ListenOrder;
import shipper.william.ffats.ViewHolder.OrdersViewHolder;

public class Home extends AppCompatActivity {
    //region Declare
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference shipperOrders;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    FirebaseRecyclerAdapter<Request, OrdersViewHolder> adapter;

    LocationCallback locationCallback;
    Location mLastLocation;

    public String location;
    Geocoder geocoder;
    List<Address> addresses;
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
    //endregion

    //region Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        insertData();
        createConstuctor();
    }

    private void createConstuctor() {
        recyclerView = findViewById(R.id.recycler_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LoadMapData(userInformation.get(SessionManager.KEY_PHONENUMBER));
        loadAllOrders(userInformation.get(SessionManager.KEY_PHONENUMBER));

    }

    private void insertData() {

        // register the service
        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);

        sessionManager = new SessionManager(Home.this, SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        database = FirebaseDatabase.getInstance();
        shipperOrders = database.getReference("PendingOrders");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            getRecentLocation();
        }else {
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

    //region Function Activity
    private void loadAllOrders(String phone) {

        DatabaseReference ordersChild = shipperOrders.child(phone);

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(ordersChild, Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrdersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrdersViewHolder holder, int position, @NonNull Request model) {

                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.txtAddress.setText(model.getAddress());
                holder.txtOrderPhone.setText(model.getPhone());
                holder.txtDate.setText("Date: " + Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));


                holder.btnShipping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder = new Intent(Home.this, TrackingOrder.class);
                        startActivity(trackingOrder);
                        Common.currentRequest = model;
                        Toast.makeText(Home.this, "okok", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @NonNull
            @Override
            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.orders_view_layout, parent, false);
                return new OrdersViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    public void LoadMapData(String phone) {
        SessionManager.MAP_VALUE = new MapValue();
        AssetManager assetManager = getAssets();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {

                // reading map data from xml to listWay and listNode
                Log.e("readXml", "run: " + GraphConstructor.getTimeToString());
                XML_reading.readXml(getAssets(), "map.osm", SessionManager.MAP_VALUE);
                Log.e("readXml", "run: " + GraphConstructor.getTimeToString());

                // remove Node that not contain any way
                GraphConstructor.removeBlankNode(SessionManager.MAP_VALUE.getNodes());

                // calculate vertices and graph
                Log.e("graphConstruction", "run: " + GraphConstructor.getTimeToString());
                GraphConstructor.graphConstructor(SessionManager.MAP_VALUE);
                Log.e("graphConstruction", "run: " + GraphConstructor.getTimeToString());


                //set view
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // chay het adapter roi set btn sang maps activity thanh visible
                        DatabaseReference ordersChild = shipperOrders.child(phone);

                        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                                .setQuery(ordersChild, Request.class)
                                .build();
                        adapter = new FirebaseRecyclerAdapter<Request, OrdersViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull OrdersViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Request model) {
                                holder.btnShipping.setEnabled(true);

                                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                                holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                                holder.txtAddress.setText(model.getAddress());
                                holder.txtOrderPhone.setText(model.getPhone());
                                holder.txtDate.setText("Date: " + Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));


                                holder.btnShipping.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Common.createLocationRealTime(adapter.getRef(position).getKey(),
                                                userInformation.get(SessionManager.KEY_PHONENUMBER),
                                                mLastLocation);

                                        Common.currentRequest = model;
                                        Common.KEY_REALTIME = adapter.getRef(position).getKey();
                                        Intent trackingOrder = new Intent(Home.this, TrackingOrder.class);
                                        startActivity(trackingOrder);
                                    }
                                });

                            }

                            @NonNull
                            @Override
                            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_view_layout, parent, false);
                                return new OrdersViewHolder(view);
                            }
                        };
                        adapter.startListening();
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
        th.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrders(userInformation.get(SessionManager.KEY_PHONENUMBER));
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

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLCAEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(Home.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION)
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
                                connectionResult.startResolutionForResult(Home.this, PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(Home.this, "bug", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addApi(API)
                .build();

        mGoogleApiClient.connect();
    }

    private void checkPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this,
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

    public void getRecentLocation(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();
                mLastLocation = LastLocation;
            }

        };
    }
    //endregion
}