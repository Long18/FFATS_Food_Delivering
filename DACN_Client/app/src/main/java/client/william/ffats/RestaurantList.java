package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import client.william.ffats.Common.Common;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Model.Category;
import client.william.ffats.Model.Restaurant;
import client.william.ffats.ViewHolder.MenuViewHolder;
import client.william.ffats.ViewHolder.RestaurantViewHolder;

public class RestaurantList extends AppCompatActivity {

    AlertDialog waitingDialog;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwiperRefresh;

    FirebaseDatabase database;
    DatabaseReference restaurant;

    FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder> adapter;

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        insertData();
        createConstructor();
    }

    @SuppressLint("ResourceAsColor")
    private void createConstructor() {

        //Swipe to reload page
        mSwiperRefresh = findViewById(R.id.swipe_layout_res);
        mSwiperRefresh.setColorSchemeColors(R.color.colorPrimary,
                android.R.color.holo_purple,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwiperRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadRes();
                else {
                    Toast.makeText(RestaurantList.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        mSwiperRefresh.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadRes();
                else {
                    Toast.makeText(RestaurantList.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        // Load menu
        recyclerView = findViewById(R.id.recycler_res);
        recyclerView.setLayoutManager(new GridLayoutManager(RestaurantList.this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),R.anim.anim_layout_fall_down);
        recyclerView.setLayoutAnimation(controller);
    }

    private void insertData() {
        database = FirebaseDatabase.getInstance();
        restaurant = database.getReference("Restaurants");
        restaurant.keepSynced(true);

        FirebaseRecyclerOptions<Restaurant> options =
                new FirebaseRecyclerOptions.Builder<Restaurant>()
                        .setQuery(restaurant, Restaurant.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RestaurantViewHolder restaurantViewHolder, int position,
                                            @NonNull final Restaurant model) {

                //set Image default
                Picasso.get().load(model.getImage()).placeholder(R.drawable.circle_dialog).fit().into(restaurantViewHolder.imageView);

                restaurantViewHolder.txtRes.setText(model.getName());
                //final Category clickItem = category;
                restaurantViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // get category id and send it to the new activity.
                        Intent foodIntent = new Intent(RestaurantList.this, FoodList.class);
                        Common.resSelected = adapter.getRef(position).getKey();
                        startActivity(foodIntent);
                    }
                });
            }


            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item, parent, false);
                return new RestaurantViewHolder(view);
            }
        };
    }
    //endregion

    //region Function
    private void loadRes(){
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        mSwiperRefresh.setRefreshing(false);

        //Anim
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
    //endregion
}