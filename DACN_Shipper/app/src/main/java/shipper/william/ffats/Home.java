package shipper.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import shipper.william.ffats.Common.Common;
import shipper.william.ffats.Database.SessionManager;
import shipper.william.ffats.Maps.GraphConstructor;
import shipper.william.ffats.Maps.MapValue;
import shipper.william.ffats.Maps.XML_reading;
import shipper.william.ffats.Model.Request;
import shipper.william.ffats.Service.ListenOrder;
import shipper.william.ffats.ViewHolder.OrdersViewHolder;

public class Home extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference shipperOrders;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    FirebaseRecyclerAdapter<Request, OrdersViewHolder> adapter;

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
        sessionManager = new SessionManager(Home.this, SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        database = FirebaseDatabase.getInstance();
        shipperOrders = database.getReference("PendingOrders");

        // register the service
        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);
    }


    private void loadAllOrders(String phone) {

        DatabaseReference ordersChild = shipperOrders.child(phone);

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(ordersChild,Request.class)
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
                        /*Intent trackingOrder = new Intent(Home.this, TrackingOrder.class);
                        Common.isShipper = model;
                        startActivity(trackingOrder);*/
                        Toast.makeText(Home.this, "okok", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @NonNull
            @Override
            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.orders_view_layout,parent,false);
                return new OrdersViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    public void LoadMapData(String phone) {
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
                        DatabaseReference ordersChild = shipperOrders.child(phone);

                        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                                .setQuery(ordersChild,Request.class)
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

                                        Toast.makeText(Home.this, "okok", Toast.LENGTH_SHORT).show();
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
}