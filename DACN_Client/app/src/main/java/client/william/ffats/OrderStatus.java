package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import client.william.ffats.Account.Sign_In;
import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Maps.GraphConstructor;
import client.william.ffats.Maps.MapValue;
import client.william.ffats.Maps.XML_reading;
import client.william.ffats.Model.Request;
import client.william.ffats.ViewHolder.OrderViewHolder;

public class OrderStatus extends AppCompatActivity {
    //region Declare Variable
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    String phone;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;
    ProgressDialog mDialog;
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        mDialog = new ProgressDialog(OrderStatus.this);
        mDialog.setMessage("Please wait...");
        Drawable drawable = new ProgressBar(OrderStatus.this).getIndeterminateDrawable().mutate();
        drawable.setColorFilter(ContextCompat.getColor(OrderStatus.this, R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN);
        mDialog.setIndeterminateDrawable(drawable);
        mDialog.show();
        
        viewConstructor();
        LoadMapData();

    }

    private void viewConstructor() {
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();
        phone = userInformation.get(SessionManager.KEY_PHONENUMBER);

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //region Click to notification
        if (getIntent() != null) {
            loadOrders(userInformation.get(SessionManager.KEY_PHONENUMBER));
        } else {
            if (getIntent().getStringExtra("userPhone") == null) {
                loadOrders(userInformation.get(SessionManager.KEY_PHONENUMBER));
            } else {
                loadOrders(getIntent().getStringExtra("userPhone"));
            }
        }

        //endregion

    }
    //endregion

    //region Function
    public void LoadMapData() {
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
                        mDialog.dismiss();
                    }
                });
            }
        });
        th.start();
    }

    private void loadOrders(String phone) {

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions
                .Builder<Request>()
                .setQuery(requests.orderByChild("phone").equalTo(phone), Request.class).build();


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(OrderViewHolder orderViewHolder, @SuppressLint("RecyclerView") final int i, Request request) {
                orderViewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(request.getStatus()));
                orderViewHolder.txtAddress.setText(request.getAddress());
                orderViewHolder.txtOrderPhone.setText(request.getPhone());

                orderViewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapter.getItem(i).getStatus().equals("0")) {
                            deleteOrder(adapter.getRef(i).getKey());
                        } else {
                            Toast.makeText(OrderStatus.this, "You can't delete", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                orderViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Empty func to fix crash when user onClick
                        Common.KEY_REALTIME = adapter.getRef(position).getKey();
                        Intent tracking = new Intent(OrderStatus.this, TrackingOrder.class);
                        startActivity(tracking);
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

    private void deleteOrder(String key) {
        requests.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(OrderStatus.this, new StringBuffer("Order ")
                        .append(key)
                        .append(" has been deleted!").toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderStatus.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    //endregion

}