package server.william.ffats;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import server.william.ffats.Common.Common;
import server.william.ffats.Database.SessionManager;
import server.william.ffats.Maps.GraphConstructor;
import server.william.ffats.Maps.MapValue;
import server.william.ffats.Maps.XML_reading;
import server.william.ffats.Model.Notification;
import server.william.ffats.Model.Request;
import server.william.ffats.Model.Response;
import server.william.ffats.Model.Sender;
import server.william.ffats.Model.Token;
import server.william.ffats.Remote.APIService;
import server.william.ffats.ViewHolder.OrderViewHolder;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseDatabase order;
    DatabaseReference requests;

    MaterialSpinner statusSpinner,shipperSpinner;

    FusedLocationProviderClient fusedLocationProviderClient;

    APIService mService;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oder_status);

        // Firebase
        order = FirebaseDatabase.getInstance();
        requests = order.getReference("Requests");
        mService = Common.getGCMService();

        //Init
        recyclerView = findViewById(R.id.listOrder);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mDialog = new ProgressDialog(OrderStatus.this);
        mDialog.setMessage("Please wait...");
        Drawable drawable = new ProgressBar(OrderStatus.this).getIndeterminateDrawable().mutate();
        drawable.setColorFilter(ContextCompat.getColor(OrderStatus.this, R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN);
        mDialog.setIndeterminateDrawable(drawable);
        mDialog.show();

        LoadMapData();
        loadOrder();

    }


    private void loadOrder() {
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(requests, Request.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, @SuppressLint("RecyclerView")final int position, @NonNull Request model) {
                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.txtAddress.setText(model.getAddress());
                holder.txtOrderPhone.setText(model.getPhone());
                holder.txtDate.setText("Date: " + Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

                holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });

                holder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

                holder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder = new Intent(OrderStatus.this, TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });

            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
                return new OrderViewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    public void LoadMapData() {
        SessionManager.MAP_VALUE  = new MapValue();
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
                        mDialog.dismiss();
                    }
                });
            }
        });
        th.start();
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


    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order,null);

        statusSpinner = view.findViewById(R.id.statusSpiner);
        statusSpinner.setItems("Placed","On my way", "Shipping");

        shipperSpinner = view.findViewById(R.id.shippersSpiner);

        //Get shippers
        List<String> shippersList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Shippers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot shippersSnapShot:snapshot.getChildren()){
                            shippersList.add(shippersSnapShot.getKey());
                        }
                        shipperSpinner.setItems(shippersList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                item.setStatus(String.valueOf(statusSpinner.getSelectedIndex()));

                if (item.getStatus().equals("2")){

                    FirebaseDatabase.getInstance().getReference("PendingOrders")
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);

                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();

                    //sendOrderStatustoUser(localKey,item);
                    //sendPendingOrderStatustoShipper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(),item);

                }else {
                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();

                    //sendOrderStatustoUser(localKey,item);
                }


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void sendPendingOrderStatustoShipper(final String phone,Request item){
        DatabaseReference tokens = order.getReference("Tokens");

        tokens.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapShop:snapshot.getChildren()){
                            Token token = postSnapShop.getValue(Token.class);

                            Notification notification = new Notification("William", "You have new order");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<Response>() {
                                        @Override
                                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                            if (response.body().success == 1){
                                                Toast.makeText(OrderStatus.this, "Sent to shippers", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(OrderStatus.this, "failed", Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Response> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void sendOrderStatustoUser(final String key,Request item) {
        DatabaseReference tokens = order.getReference("Tokens");

        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapShop:snapshot.getChildren()){
                            Token token = postSnapShop.getValue(Token.class);

                            Notification notification = new Notification("William", "Your order "+key+" was updated");
                            Sender content = new Sender(token.getToken(),notification);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<Response>() {
                                        @Override
                                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                            if (response.body().success == 1){
                                                Toast.makeText(OrderStatus.this, "Order was updated", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(OrderStatus.this, "Order was updated but failed", Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Response> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}