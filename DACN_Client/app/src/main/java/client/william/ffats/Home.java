package client.william.ffats;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import client.william.ffats.Common.Common;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Model.Category;
import client.william.ffats.ViewHolder.MenuViewHolder;
import client.william.ffats.databinding.ActivityHomeBinding;

//public class Home extends AppCompatActivity
//        implements NavigationView.OnNavigationItemSelectedListener{
//
//    private AppBarConfiguration mAppBarConfiguration;
//    private ActivityHomeBinding binding;
//    NavigationView navigationView;
//    DrawerLayout drawer;
//
//
//    FirebaseDatabase database;
//    DatabaseReference category;
//
//    TextView txtFullName;
//
//    RecyclerView recycler_menu;
//    RecyclerView.LayoutManager layoutManager;
//
//    FirebaseRecyclerAdapter adapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        //Init Firebase
//        database = FirebaseDatabase.getInstance();
//        category = database.getReference("categories");
//
//
//        binding = ActivityHomeBinding.inflate(getLayoutInflater());
//
//        setContentView(binding.getRoot());
//        setSupportActionBar(binding.appBarHome.toolbar);
//        binding.appBarHome.toolbar.setTitle("Menu");
//
//
//        binding.appBarHome.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent cartIntent = new Intent(Home.this,Cart.class);
//                startActivity(cartIntent);
//            }
//        });
//        drawer = findViewById(R.id.drawer_layout);
//        navigationView = findViewById(R.id.nav_view);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, binding.appBarHome.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//        navigationView.setNavigationItemSelectedListener(this);
//
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
//                .setOpenableLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
//
//        //Set Username for user
//        View headerView = navigationView.getHeaderView(0);
//        txtFullName = headerView.findViewById(R.id.txtFullName);
//        txtFullName.setText(Common.currentUser.getName());
//
//        //Load menu
//        recycler_menu = findViewById(R.id.recycler_menu);
//        recycler_menu.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(this);
//        recycler_menu.setLayoutManager(layoutManager);
//
//
//        if (Common.isConnectedToInternet(getBaseContext()))
//            loadMenu();
//        else {
//            Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
//            return;
//        }
//        // register the service
//        //Intent service = new Intent(Home.this, ListenOrder.class);
//        //startService(service);
//
//
//
//
//    }
//
//    private void loadMenu() {
//        FirebaseRecyclerOptions<Category> options =
//                new FirebaseRecyclerOptions.Builder<Category>()
//                        .setQuery(category, Category.class)
//                        .build();
//
//        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {
//                holder.txtMenuName.setText(model.getName());
//                Picasso.get().load(model.getImage())
//                        .into(holder.imageView);
//                Category clickItem = model;
//                holder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View view, int position, boolean isLongClick) {
//                        //Get CategoryId to new Activity
//                        Intent foodList = new Intent(Home.this,FoodList.class);
//                        //CategoryId = Key ==> Get key to see this item
//                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
//                        startActivity(foodList);
//                    }
//                });
//
//            }
//
//            @NonNull
//            @Override
//            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);
//                return new MenuViewHolder(view);
//            }
//        };
//        recycler_menu.setLayoutManager(new LinearLayoutManager(this));
//        recycler_menu.setAdapter(adapter);
//        adapter.startListening();
//    }
//
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//
//        } else {
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
//            super.onBackPressed();
//        }
//
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }
//
//
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_home) {
//            Intent menuIntent = new Intent(Home.this, Home.class);
//            startActivity(menuIntent);
//
//        } else if (id == R.id.nav_favorites) {
//            Toast.makeText(Home.this, "okok", Toast.LENGTH_SHORT).show();
//
//        }else if (id == R.id.nav_payment) {
//            Intent cartIntent = new Intent(Home.this, Cart.class);
//            startActivity(cartIntent);
//
//        } else if (id == R.id.nav_history_order) {
//            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
//            startActivity(orderIntent);
//
//        } else if (id == R.id.nav_support) {
//
//            // delete remember user and password
//            //Paper.book().destroy();
//
//            Intent mainActivity = new Intent(Home.this, MainActivity.class);
//            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(mainActivity);
//
//        }
//        drawer = findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
//}

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView TextFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager linearLayoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("categories");
        category.keepSynced(true);

        // init paper
        //Paper.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add to cart
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        //set name for user
        View headerView = navigationView.getHeaderView(0);
        TextFullName = headerView.findViewById(R.id.txtFullName);
        TextFullName.setText(Common.currentUser.getName());

        // Load menu
        recycler_menu = findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(linearLayoutManager);

        if (Common.isConnectedToInternet(getBaseContext()))
            loadMenu();
        else {
            Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
            return;
        }
        // register the service
//        Intent service = new Intent(Home.this, ListenOrder.class);
//        startService(service);
    }

    private void loadMenu() {
        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(category, Category.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MenuViewHolder menuViewHolder, int i,
                                            @NonNull final Category category) {


                Picasso.get().load(category.getImage()).placeholder(R.drawable.circle_dialog).fit().into(menuViewHolder.imageView);

                menuViewHolder.txtMenuName.setText(category.getName());
//                final Category clickItem = category;
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
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            Toast.makeText(Home.this, "okok", Toast.LENGTH_SHORT).show();

        }else if (id == R.id.nav_payment) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_history_order) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_support) {

            // delete remember user and password
            //Paper.book().destroy();

            Intent mainActivity = new Intent(Home.this, MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivity);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}