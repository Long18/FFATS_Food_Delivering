package client.william.ffats;

import static com.google.android.gms.location.LocationServices.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import vn.momo.momo_partner.AppMoMoLib;

public class Cart extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RecyclerItemTouchListener{
    //region Declare Variable
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    Button btnPlace;

    RadioButton rdbToAddress, rdbToHome,rdbCash,rdbMomo;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    String location;

    RelativeLayout rootLayout;

    IGoogleService mGoogleMapService;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    APIService mService;

    MaterialEditText edtComment, edtAddress;

    PlacesClient placesClient;
    Place address;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS);
    AutocompleteSupportFragment places_fragment;

    //private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    Location mLastLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    Geocoder geocoder;
    List<Address> addresses;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLCAEMENT = 10;

    private static final int LOCATION_REQUEST = 7777;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 7000;
    private static final int LOCATION_PERMISSION_REQUEST = 7007;

    private LocationResolver mLocationResolver;

    private String amount = "10000";
    private String fee = "0";
    int environment = 0;//developer default
    private String merchantName = "FFATS";
    private String merchantCode = "MOMOISIA20211130";
    private String merchantNameLabel = "Nhà cung cấp";
    private String description = "Thanh toán dịch vụ FFAST";
    String orderNumber;

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
            if (buttonView.getId() == R.id.rdbShipToAdress) {
                if (isChecked) {
                    edtAddress.setText(location);
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

            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {

                createLocationRequest();
                buildGoogleApiClient();

                loadListFood();
                checkPermissionLocation();
                displayLocation();

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }

        //Swipe delete
        ItemTouchHelper.SimpleCallback iSimpleCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, Cart.this);
        new ItemTouchHelper(iSimpleCallback).attachToRecyclerView(recyclerView);
    }

    private void insertData() {
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);

        checkPermissionLocation();
        // Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        mService = Common.getGCMService();

        Places.initialize(this, getString(R.string.ffats_places));
        placesClient = Places.createClient(this);
    }
    //endregion

    //region Function

    //Get token through MoMo app
    private void requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        if (txtTotalPrice.getText().toString() != null && txtTotalPrice.getText().toString().trim().length() != 0)
            amount = txtTotalPrice.getText().toString().trim();

        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put("FFATS", merchantName); //Tên đối tác. được đăng ký tại https://business.momo.vn. VD: Google, Apple, Tiki , CGV Cinemas
        eventValue.put("merchantcode", merchantCode); //Mã đối tác, được cung cấp bởi MoMo tại https://business.momo.vn
        eventValue.put("amount", txtTotalPrice.getText().toString()); //Kiểu integer
        eventValue.put("orderId", orderNumber); //uniqueue id cho Bill order, giá trị duy nhất cho mỗi đơn hàng
        eventValue.put("orderLabel", "Mã đơn hàng"); //gán nhãn

        //client Optional - bill info
        eventValue.put("merchantnamelabel", "Dịch vụ");//gán nhãn
        eventValue.put("fee", txtTotalPrice.getText().toString()); //Kiểu integer
        eventValue.put("description", description); //mô tả đơn hàng - short description

        //client extra data
        eventValue.put("requestId",  merchantCode+"merchant_billId_"+System.currentTimeMillis());
        eventValue.put("partnerCode", merchantCode);
        //Example extra data
        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3");
            objExtraData.put("movie_format", "2D");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put("extraData", objExtraData.toString());

        eventValue.put("extra", "");
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);


    }
    //Get token callback from MoMo app an submit to server side
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if(data != null) {
                if(data.getIntExtra("status", -1) == 0) {
                    //TOKEN IS AVAILABLE
                    Toast.makeText(Cart.this,"Get token " + data.getStringExtra("message"),Toast.LENGTH_SHORT).show();
                    String token = data.getStringExtra("data"); //Token response
                    String phoneNumber = data.getStringExtra("phonenumber");
                    String env = data.getStringExtra("env");
                    if(env == null){
                        env = "app";
                    }

                    if(token != null && !token.equals("")) {
                        // TODO: send phoneNumber & token to your server side to process payment with MoMo server
                        // IF Momo topup success, continue to process your order
                    } else {
                        Toast.makeText(Cart.this,"message: " + this.getString(R.string.not_receive_info),Toast.LENGTH_SHORT).show();
                    }
                } else if(data.getIntExtra("status", -1) == 1) {
                    //TOKEN FAIL
                    String message = data.getStringExtra("message") != null?data.getStringExtra("message"):"Thất bại";
                    Toast.makeText(Cart.this,"message: " + message,Toast.LENGTH_SHORT).show();
                } else if(data.getIntExtra("status", -1) == 2) {
                    //TOKEN FAIL
                    Toast.makeText(Cart.this,"message: " + this.getString(R.string.not_receive_info),Toast.LENGTH_SHORT).show();
                } else {
                    //TOKEN FAIL
                    Toast.makeText(Cart.this,"message: " + this.getString(R.string.not_receive_info),Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Cart.this,"message: " + this.getString(R.string.not_receive_info),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Cart.this,"message: " + this.getString(R.string.not_receive_info_err),Toast.LENGTH_SHORT).show();
        }
    }


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
        //setupPlaceAutocomplete();
        edtComment = order_address.findViewById(R.id.edtComment);
        edtAddress = order_address.findViewById(R.id.edtAddress);

        rdbToHome = (RadioButton) order_address.findViewById(R.id.rdbHomeAdress);
        rdbToAddress = (RadioButton) order_address.findViewById(R.id.rdbShipToAdress);
        rdbCash = (RadioButton) order_address.findViewById(R.id.rdbCash);
        rdbMomo = (RadioButton) order_address.findViewById(R.id.rdbMomoPayment);

        rdbToAddress.setOnCheckedChangeListener(onCheckedChangeListener);
        rdbToHome.setOnCheckedChangeListener(onCheckedChangeListener);
        rdbCash.setOnCheckedChangeListener(onCheckedChangeListener);
        rdbMomo.setOnCheckedChangeListener(onCheckedChangeListener);

        alertDialog.setIcon(R.drawable.shopping_basket);

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*Request request = new Request(
                        userInformation.get(SessionManager.KEY_PHONENUMBER),
                        userInformation.get(SessionManager.KEY_FULLNAME),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString(),
                        "momo",
                        cart
                );
                // Submit to firebase
                //we will using System.Current to key
                String orderNumber = String.valueOf(System.currentTimeMillis());
                requests.child(orderNumber).setValue(request);
                //deleting cart
                new Database(getBaseContext()).cleanCart(userInformation.get(SessionManager.KEY_PHONENUMBER));
*/

//                //remove fragment
//                getFragmentManager().beginTransaction().remove(
//                        getFragmentManager().findFragmentById(R.id.fragment_address))
//                        .commit();

                if (!rdbToAddress.isChecked() && !rdbToHome.isChecked()){
                    if (edtAddress.getText() != null){
                        edtAddress.setText(location);
                    }else {
                        Toast.makeText(Cart.this, "Please enter address!",Toast.LENGTH_SHORT).show();

                        return;
                    }
                }

                if (!rdbCash.isChecked() && !rdbMomo.isChecked()){

                    Toast.makeText(Cart.this, "Please select payment method!",Toast.LENGTH_SHORT).show();
                    return;

                }else if (rdbMomo.isChecked()){
                    Request request = new Request(
                            userInformation.get(SessionManager.KEY_PHONENUMBER),
                            userInformation.get(SessionManager.KEY_FULLNAME),
                            edtAddress.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            "0",
                            edtComment.getText().toString(),
                            "Momo",
                            cart
                    );
                    // Submit to firebase
                    //we will using System.Current to key
                    orderNumber = String.valueOf(System.currentTimeMillis());
                    requests.child(orderNumber).setValue(request);
                    //deleting cart
                    new Database(getBaseContext()).cleanCart(userInformation.get(SessionManager.KEY_PHONENUMBER));


                    requestPayment();

                    sendNotification(orderNumber);
                    Intent home = new Intent(Cart.this, Home.class);
                    Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                    startActivity(home);
                    finish();

                }else if (rdbCash.isChecked()){
                    Request request = new Request(
                            userInformation.get(SessionManager.KEY_PHONENUMBER),
                            userInformation.get(SessionManager.KEY_FULLNAME),
                            edtAddress.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            "0",
                            edtComment.getText().toString(),
                            "Cash",
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
                }


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

//                //remove fragment
//                getFragmentManager().beginTransaction().remove(
//                        getFragmentManager().findFragmentById(R.id.fragment_address))
//                        .commit();
            }
        });
        alertDialog.show();

    }

//    private void setupPlaceAutocomplete() {
//        //MaterialEditText fragment_address = order_address.findViewById(R.id.edtAdress);
//        //PlaceAutocompleteFragment fragment_address = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_address);
//        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_address);
//        places_fragment.setPlaceFields(placeFields);
//
//        //Hide search icon
//        //places_fragment.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
//        //Hint for auto complete
//        //((EditText)places_fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Enter your address");
//        //Set Text Size
//        //((EditText)places_fragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14);
//        //Get address from autocomplete
//        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onError(@NonNull Status status) {
//                Toast.makeText(Cart.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
//                Log.e("Erorr", status.getStatusMessage());
//            }
//
//            @Override
//            public void onPlaceSelected(@NonNull Place place) {
//                Toast.makeText(Cart.this, "" + place.getName(), Toast.LENGTH_SHORT).show();
//                address = place;
//            }
//        });
//    }

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

    public void loadListFood() {
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
        if (viewHolder instanceof CartViewHolder) {
            String name = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeCart(deleteItem.getProductId(), userInformation.get(SessionManager.KEY_PHONENUMBER));

            //calculating total price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCart(userInformation.get(SessionManager.KEY_PHONENUMBER));
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("vn", "VN");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));

            //Popup Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + "removed from cart", Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
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
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLCAEMENT);
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

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, LOCATION_PERMISSION_REQUEST);
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
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location LastLocation = locationResult.getLastLocation();

                Toast.makeText(Cart.this, LastLocation.getLatitude() + "," +LastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                if (LastLocation != null) {

                    try {
                        geocoder = new Geocoder(Cart.this, Locale.getDefault());
                        addresses = geocoder.getFromLocation(LastLocation.getLatitude(),LastLocation.getLongitude(),1);


                        double latitude = LastLocation.getLatitude();
                        double longitude = LastLocation.getLongitude();

                        location = addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast.makeText(Cart.this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
                }
            }

        };
        /*if (mLastLocation != null){
            Log.d("Your Location:", mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
            Toast.makeText(Cart.this, mLastLocation.getLatitude()+","+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        }else {
            Log.d("Location:", "Couldn't find");
            Toast.makeText(Cart.this, "Đéo thấy", Toast.LENGTH_SHORT).show();
        }*/
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        } else {
            FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationRequest,this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //mLastLocation = location;
        //displayLocation();
    }

    //endregion
}