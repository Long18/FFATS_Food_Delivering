package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.Database;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Model.Favorites;
import client.william.ffats.Model.Food;
import client.william.ffats.Model.Order;
import client.william.ffats.ViewHolder.FoodViewHolder;

public class Search extends AppCompatActivity {
    //region Declare Variable
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    //Search
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter,adapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar searchBar;

    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //Get image form bitmap to fb
    Target facabookShare = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(Search.this,content);
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        getData();
        viewConstructor();
    }

    private void getData() {
        //DB
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Restaurants").child(Common.resSelected).child("detail").child("Foods");
        localDB = new Database(this);
        //Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
    }

    private void viewConstructor() {
        recyclerView = findViewById(R.id.recycler_search);
        recyclerView.hasFixedSize();
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),R.anim.anim_layout_fall_down);
        recyclerView.setLayoutAnimation(controller);

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();



        //region Search Function
        searchBar = findViewById(R.id.searchBar);
        searchBar.setHint("Enter your food");
        loadSuggest();
        searchBar.setLastSuggestions(suggestList);
        searchBar.setCardViewElevation(10);
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search bar iss close ==> return original adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish ==> show result
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //When user typing ==> suggest
                List<String> suggest = new ArrayList<>();
                for (String search:suggestList) //loop info suggest
                {
                    if (search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        loadAllFoods();
        //endregion
    }
    //endregion

    //region Function
    private void loadAllFoods() {

        // Create query by Category Id
        Query searchByName = foodList;
        // create options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class).build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, @SuppressLint("RecyclerView") final int position,
                                            @NonNull final Food model) {

                viewHolder.txtFoodName.setText(model.getName());
                viewHolder.txtFoodPrice.setText(String.format("%s đ",model.getPrice().toString()));

                Picasso.get().load(model.getImage())
                        .into(viewHolder.imageFood);

                boolean isExists = new Database(getBaseContext())
                        .checkFoodExists(adapter.getRef(position).getKey(),userInformation.get(SessionManager.KEY_PHONENUMBER));
                viewHolder.btnQuickCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    userInformation.get(SessionManager.KEY_PHONENUMBER),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));
                        }else {
                            new Database(getBaseContext())
                                    .increaseCart(userInformation.get(SessionManager.KEY_PHONENUMBER),adapter.getRef(position).getKey());
                        }
                        Toast.makeText(Search.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    }
                });


                //Click share
                viewHolder.fav_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.get().load(model.getImage())
                                .into(facabookShare);

                        Toast.makeText(Search.this, "okok", Toast.LENGTH_SHORT).show();
                    }
                });

                //Add favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey(),userInformation.get(SessionManager.KEY_PHONENUMBER))){
                    viewHolder.fav_image.setImageResource(R.drawable.heart_filled);
                }

                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setFoodPrice(model.getPrice());
                        favorites.setUserPhone(userInformation.get(SessionManager.KEY_PHONENUMBER));

                        if (!localDB.isFavorites(adapter.getRef(position).getKey(),userInformation.get(SessionManager.KEY_PHONENUMBER))){
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.heart_filled);
                            Toast.makeText(Search.this, model.getName() + " was added to favorites", Toast.LENGTH_SHORT).show();
                        }else{
                            localDB.removeFavorites(adapter.getRef(position).getKey(),userInformation.get(SessionManager.KEY_PHONENUMBER));
                            viewHolder.fav_image.setImageResource(R.drawable.heart);
                            Toast.makeText(Search.this, model.getName() + " was remove to favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(Search.this, FoodDetail.class);
                        //Send Id to new activity
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setLayoutManager(new GridLayoutManager(Search.this,2));
        recyclerView.setAdapter(adapter);
    }

    private void startSearch(CharSequence text) {

        // Create query by Category Id
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        // create options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class).build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.txtFoodName.setText(model.getName());
                Picasso.get().load(model.getImage())
                        .into(holder.imageFood);

                final Food local = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(Search.this, FoodDetail.class);
                        //Send Id to new activity
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setLayoutManager(new GridLayoutManager(Search.this,2));
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        foodList.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot:snapshot.getChildren()){
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName()); //Add name to suggest list
                        }
                        searchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    //endregion
}