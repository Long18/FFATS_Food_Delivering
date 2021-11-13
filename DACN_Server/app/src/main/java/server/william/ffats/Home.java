package server.william.ffats;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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

import java.util.HashMap;
import java.util.UUID;

import server.william.ffats.Common.Common;
import server.william.ffats.Database.SessionManager;
import server.william.ffats.Interface.ItemClickListener;
import server.william.ffats.Model.Category;
import server.william.ffats.Model.Token;
import server.william.ffats.Service.ListenOrder;
import server.william.ffats.ViewHolder.MenuViewHolder;
import server.william.ffats.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView TextFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager linearLayoutManager;

    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    TextInputLayout editName;
    Button btnUpload, btnSelect;

    Category newCategory;

    Uri saveUri;
    DrawerLayout drawer;


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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // init paper
        //Paper.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        //set name for user
        View headerView = navigationView.getHeaderView(0);
        TextFullName = headerView.findViewById(R.id.txtFullName);

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();
        TextFullName.setText(userInformation.get(SessionManager.KEY_FULLNAME));

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
        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);

        String token = FirebaseMessaging.getInstance().getToken().toString();
        updateToken(token);
    }

    private void updateToken(String token) {

        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,true);
        tokens.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).setValue(data);
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Add new Category");
        alertDialog.setMessage("Please write the name of foods");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu = inflater.inflate(R.layout.add_new_menu_layout,null);

        editName = add_menu.findViewById(R.id.editName);
        btnSelect = add_menu.findViewById(R.id.btnSelect);
        btnUpload = add_menu.findViewById(R.id.btnUpload);

        //Button event
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image and save to store firebase
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu);
        alertDialog.setIcon(R.drawable.shopping_basket);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Create new category
                if (newCategory != null){
                    category.push().setValue(newCategory);
                    Snackbar.make(drawer,"New category " +newCategory.getName()+"was added.", Snackbar.LENGTH_SHORT).show();
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

    private void uploadImage() {
        if (saveUri != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this,"Success!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set value for new Category if image upload
                                    newCategory = new Category(editName.getEditText().getText()
                                    .toString(),uri.toString());
                                }
                            });
                        }
                    }) .addOnFailureListener(e -> {
                mDialog.dismiss();
                Toast.makeText(Home.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            mDialog.setMessage("Upload Success" + progress + "%");
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent,"Select Image"),Common.CHOOSE_IMAGE_REQUEST);
    }

    private void changeImage(final Category item) {
        if (saveUri != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this,"Success!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set value for new Category if image upload
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    }) .addOnFailureListener(e -> {
                mDialog.dismiss();
                Toast.makeText(Home.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            mDialog.setMessage("Upload Success" + progress + "%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK
        && data != null && data.getData() != null){
            saveUri = data.getData();
            btnSelect.setText("Image Selected!");
        }
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
            Intent cartIntent = new Intent(Home.this, OrderStatus.class);
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

    //Update&Delete Func

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)){
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {
        DatabaseReference foods = database.getReference("Foods");
        Query foodInCategory = foods.orderByChild("menuId").equalTo(key);
        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        category.child(key).removeValue();
        Toast.makeText(Home.this, "Delete Success", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(String key, Category item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please write the name of foods");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu = inflater.inflate(R.layout.add_new_menu_layout,null);

        editName = add_menu.findViewById(R.id.editName);
        btnSelect = add_menu.findViewById(R.id.btnSelect);
        btnUpload = add_menu.findViewById(R.id.btnUpload);

        //Set default name
        editName.getEditText().setText(item.getName());

        //Button event
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image and save to store firebase
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu);
        alertDialog.setIcon(R.drawable.shopping_basket);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

               item.setName(editName.getEditText().getText().toString());
               category.child(key).setValue(item);
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

}