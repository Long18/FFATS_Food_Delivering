package client.william.ffats;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.squareup.picasso.Picasso;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.HashMap;
import java.util.Random;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.Database;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Model.Banner;
import client.william.ffats.Model.Category;
import client.william.ffats.Model.Token;
import client.william.ffats.Remote.LocationResolver;
import client.william.ffats.Service.ListenOrder;
import client.william.ffats.ViewHolder.MenuViewHolder;
import io.paperdb.Paper;



public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //region Declare Variable
    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtCity;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager linearLayoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    CardView cardSearch;

    ImageView btnMenu;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    HashMap<String,String> images_list;
    SliderLayout mSlider;
    int maximum = 8000;
    int minimum = 6000;
    int randomNum;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Add to cart
            if (v.getId() == R.id.fab){
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);

            }
            //endregion

            //region Menu
            if (v.getId() == R.id.ic_menu){
                Intent openMenu = new Intent(Home.this, client.william.ffats.Menu.class);
                startActivity(openMenu);

            }
            //endregion

        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        insertData();
        viewConstructor();

    }

    @SuppressLint("ResourceAsColor")
    private void viewConstructor() {
        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        btnMenu = findViewById(R.id.ic_menu);
        txtCity = findViewById(R.id.txtCity);

        btnMenu.setOnClickListener(onClickListener);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);*/

        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);*/

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(onClickListener);
        fab.setCount(new Database(getBaseContext()).getCountCart(userInformation.get(SessionManager.KEY_PHONENUMBER)));


        //set name for user
        //View headerView = navigationView.getHeaderView(0);
        //TextFullName = headerView.findViewById(R.id.txtFullName);
        //txtSeeMore = headerView.findViewById(R.id.txtSeeMore);

        cardSearch = findViewById(R.id.card_search);
        cardSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search = new Intent(Home.this, Search.class);
                startActivity(search);
            }
        });

        txtCity.setText(userInformation.get(SessionManager.KEY_CITY));
        //txtCity.setText(MainActivity);
//        TextFullName.setText(userInformation.get(SessionManager.KEY_FULLNAME));


        // Load menu
        recycler_menu = findViewById(R.id.recycler_menu);
        //recycler_menu.setHasFixedSize(true);
        //linearLayoutManager = new LinearLayoutManager(this);
        //recycler_menu.setLayoutManager(linearLayoutManager);
        recycler_menu.setLayoutManager(new GridLayoutManager(Home.this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),R.anim.anim_layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);

        //Swipe to reload page
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary,
                android.R.color.holo_purple,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        //region Check Internet
        if (Common.isConnectedToInternet(getBaseContext()))
            loadMenu();
        else {
            Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
            return;
        }
        //endregion

        String token = FirebaseMessaging.getInstance().getToken().toString();

        Intent intent = new Intent(Home.this, ListenOrder.class);
        startService(intent);

        updateToken(token);

        //Slider
        loadSlider();

    }

    private void insertData() {


        database = FirebaseDatabase.getInstance();
        category = database.getReference("categories");
        category.keepSynced(true);

        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(category, Category.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MenuViewHolder menuViewHolder, int position,
                                            @NonNull final Category model) {

                //set Image default
                Picasso.get().load(model.getImage()).placeholder(R.drawable.circle_dialog).fit().into(menuViewHolder.imageView);

                menuViewHolder.txtMenuName.setText(model.getName());
                //final Category clickItem = category;
                menuViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // get category id and send it to the new activity.
                        Intent foodIntent = new Intent(Home.this, FoodList.class);
                        // category id is key,we just get the key of this item
                        foodIntent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodIntent);
                    }
                });
            }


            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(view);
            }
        };

        Paper.init(this);
    }
    //endregion

    //region Function

    private void loadSlider() {
        mSlider = findViewById(R.id.slider);
        images_list = new HashMap<>();

        final DatabaseReference slider = database.getReference("Restaurants").child(Common.resSelected).child("detail").child("Banner");

        slider.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapShot:snapshot.getChildren()){
                    Banner banner = postSnapShot.getValue(Banner.class);
                    images_list.put(banner.getName()+"@@@"+banner.getId(), banner.getImage());
                }
                for (String key:images_list.keySet()){
                    String[] keySplit = key.split("@@@");
                    String foodName = keySplit[0];
                    String foodId = keySplit[1];

                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView.setPicasso(Picasso.get());
                    textSliderView.description(foodName)
                            .image(images_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this,FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",foodId);
                    mSlider.addSlider(textSliderView);

                    slider.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        Random rn = new Random();
        int n = maximum - minimum + 1;
        int i = rn.nextInt() % n;
        randomNum =  minimum + i;
        mSlider.setDuration(randomNum);
    }

    private void loadMenu() {

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Anim
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(Home.this).getCountCart(userInformation.get(SessionManager.KEY_PHONENUMBER)));
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            super.onBackPressed();
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        fab.setCount(new Database(Home.this).getCountCart(userInformation.get(SessionManager.KEY_PHONENUMBER)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh)
            loadMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent menuIntent = new Intent(Home.this, Home.class);
            startActivity(menuIntent);

        } else if (id == R.id.nav_favorites) {
            Intent fav = new Intent(Home.this, FavoritesActivity.class);
            startActivity(fav);

        }else if (id == R.id.nav_payment) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_history_order) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_support) {

            // delete remember user and password

            SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
            sessionManager.checkUserLogout();

            Paper.book().destroy();

            Intent mainActivity = new Intent(Home.this, MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivity);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateToken(String token){

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false);
        tokens.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).setValue(data);
    }

    //endregion
}