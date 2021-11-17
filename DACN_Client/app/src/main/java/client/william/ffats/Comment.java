package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import client.william.ffats.Common.Common;
import client.william.ffats.Model.Rate;
import client.william.ffats.ViewHolder.CommentViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Comment extends AppCompatActivity {
    //region Declare Variable
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase db;
    DatabaseReference ratingDBR;

    SwipeRefreshLayout swipeRefreshLayout;
    String foodId= "";

    FirebaseRecyclerAdapter<Rate, CommentViewHolder> adapter;
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        insertData();
        viewConstructor();
    }

    @SuppressLint("ResourceAsColor")
    private void viewConstructor() {
        recyclerView = findViewById(R.id.recycler_comment);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout = findViewById(R.id.comment_swipe);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary,
                android.R.color.holo_purple,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //region Get Intent
                if (getIntent() != null)
                    foodId = getIntent().getStringExtra("FoodId");
                if (!foodId.isEmpty() && foodId != null){
                    if (Common.isConnectedToInternet(getBaseContext())){
                        Query query = ratingDBR.orderByChild("foodId").equalTo(foodId);

                        FirebaseRecyclerOptions<Rate> options = new FirebaseRecyclerOptions.Builder<Rate>()
                                .setQuery(query,Rate.class)
                                .build();

                        adapter = new FirebaseRecyclerAdapter<Rate, CommentViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rate model) {
                                holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                                holder.txtComment.setText(model.getComment());
                                holder.txtPhone.setText(model.getUserPhone());
                            }

                            @NonNull
                            @Override
                            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.comment_layout,parent,false);
                                return new CommentViewHolder(view);
                            }
                        };
                    }else{
                        Toast.makeText(Comment.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    loadComment(foodId);
                }
                //endregion
            }
        });

        //Thread load comment
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                //region Get Intent
                if (getIntent() != null)
                    foodId = getIntent().getStringExtra("FoodId");
                if (!foodId.isEmpty() && foodId != null){
                    if (Common.isConnectedToInternet(getBaseContext())){
                        Query query = ratingDBR.orderByChild("foodId").equalTo(foodId);

                        FirebaseRecyclerOptions<Rate> options = new FirebaseRecyclerOptions.Builder<Rate>()
                                .setQuery(query,Rate.class)
                                .build();

                        adapter = new FirebaseRecyclerAdapter<Rate, CommentViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rate model) {
                                holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                                holder.txtComment.setText(model.getComment());
                                holder.txtPhone.setText(model.getUserPhone());
                            }

                            @NonNull
                            @Override
                            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.comment_layout,parent,false);
                                return new CommentViewHolder(view);
                            }
                        };
                    }else{
                        Toast.makeText(Comment.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    loadComment(foodId);
                }
                //endregion
            }
        });
    }

    private void insertData() {
        db = FirebaseDatabase.getInstance();
        ratingDBR = db.getReference("Rating");

    }
    //endregion

    //region Function
    private void loadComment(String foodId) {
        adapter.startListening();

        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }
    //endregion
}