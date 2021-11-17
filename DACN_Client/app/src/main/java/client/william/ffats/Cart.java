package client.william.ffats;

import static com.google.android.gms.location.LocationServices.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.Database;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Helper.RecyclerItemTouchHelper;
import client.william.ffats.Interface.RecyclerItemTouchListener;
import client.william.ffats.Model.Notification;
import client.william.ffats.Model.Order;
import client.william.ffats.Model.Request;
import client.william.ffats.Model.Response;
import client.william.ffats.Model.Sender;
import client.william.ffats.Model.Token;
import client.william.ffats.Remote.APIService;
import client.william.ffats.Remote.IGoogleService;
import client.william.ffats.Remote.LocationResolver;
import client.william.ffats.ViewHolder.CartAdapter;
import client.william.ffats.ViewHolder.CartViewHolder;
import retrofit2.Call;
import retrofit2.Callback;

public class Cart extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RecyclerItemTouchListener {
    //region Declare Variable
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    Button btnPlace;

    RadioButton rdbToAddress, rdbToHome;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    RelativeLayout rootLayout;

    IGoogleService mGoogleMapService;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    APIService mService;

    PlacesClient placesClient;
    Place address;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS);
    AutocompleteSupportFragment places_fragment;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLCAEMENT = 10;

    private static final int LOCATION_REQUEST = 7777;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 7000;
    private static final int LOCATION_PERMISSION_REQUEST = 7007;

    private LocationResolver mLocationResolver;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region btnCart
            if (v.getId() == R.id.btnPlaceOrder) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your Cart is Empty", Toast.LENGTH_SHORT).show();
            }
            //endregion
        }
    };

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //region rdbShiptoAddress
            if (buttonView.getId() == R.id.rdbHomeAdress) {
                if (isChecked) {
                    mGoogleMapService.getAddressName(String
                            .format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                                    mLastLocation.getLatitude(),
                                    mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());

                                        JSONArray resultArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject = resultArray.getJSONObject(0);

                                        String address = firstObject.getString("formatted_address");
                                        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_address);
                                        ((EditText) places_fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            //endregion
        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        insertData();
        viewConstructor();
        checkPermissionLocation();

    }

    private void viewConstructor() {
        //Init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);
        rootLayout = findViewById(R.id.rootLayout);

        btnPlace.setOnClickListener(onClickListener);

        mLocationResolver = new LocationResolver(this);
        mGoogleMapService = Common.getGoogleMaps();

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();


        mLocationResolver.resolveLocation(this, new LocationResolver.OnLocationResolved() {
            @Override
            public void onLocationResolved(Location location) {
                loadListFood();
                checkPermissionLocation();
                displayLocation();
            }
        });

        //Swipe delete
        ItemTouchHelper.SimpleCallback iSimpleCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,Cart.this);
        new ItemTouchHelper(iSimpleCallback).attachToRecyclerView(recyclerView);
    }

    private void insertData() {
        // Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        mService = Common.getGCMService();

        Places.initialize(this, getString(R.string.ffats_places));
        placesClient = Places.createClient(this);
    }
    //endregion

    //region Function
    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more Step!");
        alertDialog.setMessage("Enter your Address: ");

        //        final EditText edtAddress = new EditText(Cart.this);
        //        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        //                LinearLayout.LayoutParams.MATCH_PARENT,
        //                LinearLayout.LayoutParams.MATCH_PARENT
        //        );
        //        edtAddress.setLayoutParams(lp);
        //        alertDialog.setView(edtAddress);


        LayoutInflater inflater = this.getLayoutInflater();
        View order_address = inflater.inflate(R.layout.order_address_comment, null);
        alertDialog.setView(order_address);

        //Auto complete map address
        setupPlaceAutocomplete();
        MaterialEditText edtComment = order_address.findViewById(R.id.edtComment);

        rdbToHome = (RadioButton) order_address.findViewById(R.id.rdbHomeAdress);
        rdbToAddress = (RadioButton) order_address.findViewById(R.id.rdbShipToAdress);

        rdbToAddress.setOnCheckedChangeListener(onCheckedChangeListener);
        rdbToHome.setOnCheckedChangeListener(onCheckedChangeListener);

        alertDialog.setIcon(R.drawable.shopping_basket);

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Request request = new Request(
                        userInformation.get(SessionManager.KEY_PHONENUMBER),
                        userInformation.get(SessionManager.KEY_FULLNAME),
                        address.getAddress().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString(),
                        cart
                );
                // Submit to firebase
                //we will using System.Current to key
                String orderNumber = String.valueOf(System.currentTimeMillis());
                requests.child(orderNumber).setValue(request);
                //deleting cart
                new Database(getBaseContext()).cleanCart(userInformation.get(SessionManager.KEY_PHONENUMBER));

                sendNotification(orderNumber);
                Intent home = new Intent(Cart.this, Home.class);
                Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                startActivity(home);
                finish();

                //remove fragment
                getFragmentManager().beginTransaction().remove(
                        getFragmentManager().findFragmentById(R.id.fragment_address))
                        .commit();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //remove fragment
                getFragmentManager().beginTransaction().remove(
                        getFragmentManager().findFragmentById(R.id.fragment_address))
                        .commit();
            }
        });
        alertDialog.show();

    }

    private void setupPlaceAutocomplete() {
        //MaterialEditText fragment_address = order_address.findViewById(R.id.edtAdress);
        //PlaceAutocompleteFragment fragment_address = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_address);
        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_address);
        places_fragment.setPlaceFields(placeFields);

        //Hide search icon
        //places_fragment.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //Hint for auto complete
        //((EditText)places_fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Enter your address");
        //Set Text Size
        //((EditText)places_fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14);
        //Get address from autocomplete
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Cart.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Erorr", status.getStatusMessage());
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Toast.makeText(Cart.this, "" + place.getName(), Toast.LENGTH_SHORT).show();
                address = place;
            }
        });
    }

    private void sendNotification(String orderNumber) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapShop : snapshot.getChildren()) {
                    Token serverToken = postSnapShop.getValue(Token.class);

                    //Create raw payload
                    Notification notification = new Notification("William"
                            , "You have new order " + orderNumber);
                    Sender content = new Sender(serverToken.getToken(), notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    if (response.body().success == 1) {
                                        Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(Cart.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {
                                    Log.e("Error", t.getMessage());
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadListFood() {
        cart = new Database(this).getCart(userInformation.get(SessionManager.KEY_PHONENUMBER));
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculating total price
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("vn", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == Common.DELETE)
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int order) {
        // it will remove item at List<Order> by position
        cart.remove(order);
        // after that it will delete old data from SQlite
        new Database(this).cleanCart(userInformation.get(SessionManager.KEY_PHONENUMBER));
        // and finally , we will update  new data from List<order> to Sqlite
        for (Order item : cart)
            new Database(this).addToCart(item);
        //referesh
        loadListFood();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder){
            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeCart(deleteItem.getProductId(),userInformation.get(SessionManager.KEY_PHONENUMBER));

            //calculating total price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCart(userInformation.get(SessionManager.KEY_PHONENUMBER));
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("vn", "VN");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));

            //Popup Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name + "removed from cart",Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //calculating total price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCart(userInformation.get(SessionManager.KEY_PHONENUMBER));
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("vn", "VN");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private void checkPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(Cart.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Cart.this,
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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLCAEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(Cart.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(Cart.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(Cart.this, Manifest.permission.ACCESS_COARSE_LOCATION)
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
                                connectionResult.startResolutionForResult(Cart.this, PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(Cart.this, "bug", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addApi(API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
        mLastLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);


        Log.d("Check","onConnected");
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationResolver.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();

                        displayLocation();
                    }
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLastLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null){
            Log.d("Your Location:", mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
            Toast.makeText(Cart.this, mLastLocation.getLatitude()+","+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        }else {
            Log.d("Location:", "Couldn't find");
            Toast.makeText(Cart.this, "Đéo thấy", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        } else {
            FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationResolver.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationResolver.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationResolver.onDestroy();
    }
    //endregion
}