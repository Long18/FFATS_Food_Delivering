package server.william.ffats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import server.william.ffats.Common.Common;
import server.william.ffats.Model.Banner;
import server.william.ffats.Model.Food;
import server.william.ffats.ViewHolder.BannerViewHolder;

public class BannerManager extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout rootLayout;
    FloatingActionButton fab;

    FirebaseDatabase database;
    DatabaseReference banners;
    FirebaseStorage storage;
    StorageReference storageReference;
    
    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    TextInputLayout txtName,txtIdFood;
    Button btnUpload,btnSelect;
    
    Banner newBanner;
    Uri path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_manager);

        getData();
        setConstructor();
    }

    private void getData() {
        database = FirebaseDatabase.getInstance();
        banners = database.getReference("Banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void setConstructor() {
        recyclerView = findViewById(R.id.recycler_banner);
        recyclerView.hasFixedSize();
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = findViewById(R.id.rootLayout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBannerDialog();
            }
        });

        loadListBanner();

    }

    private void loadListBanner() {
        FirebaseRecyclerOptions<Banner> options = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(banners,Banner.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {
                holder.txtBannerName.setText(model.getName());
                Picasso.get().load(model.getImage())
                        .into(holder.imageBanner);
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.banner_layout,parent,false);
                return new BannerViewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void showAddBannerDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerManager.this);
        alertDialog.setTitle("Add new Banner");
        alertDialog.setMessage("Please write the name of Banner");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_banner = inflater.inflate(R.layout.add_new_banner,null);

        txtIdFood = add_banner.findViewById(R.id.editId);
        txtName = add_banner.findViewById(R.id.editName);
        btnSelect = add_banner.findViewById(R.id.btnSelect);
        btnUpload = add_banner.findViewById(R.id.btnUpload);

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

        alertDialog.setView(add_banner);
        alertDialog.setIcon(R.drawable.heart);

        //Set button
        alertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                //Create new category
                if (newBanner != null){
                    banners.push().setValue(newBanner);
                    Snackbar.make(rootLayout,"New banner " +newBanner.getName()+"was added.", Snackbar.LENGTH_SHORT).show();
                    loadListBanner();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newBanner = null;
                dialog.dismiss();
                loadListBanner();
            }
        });
        alertDialog.show();

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent,"Select Image"), Common.CHOOSE_IMAGE_REQUEST);
    }

    private void changeImage(final Banner item) {

        if (path != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerManager.this,"Success!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(BannerManager.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void uploadImage() {
        if (path != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerManager.this,"Success!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set value for new Category if image upload
                                    newBanner = new Banner();
                                    newBanner.setName(txtName.getEditText().getText().toString());
                                    newBanner.setId(txtIdFood.getEditText().getText().toString());
                                    newBanner.setImage(uri.toString());

                                }
                            });
                        }
                    }) .addOnFailureListener(e -> {
                mDialog.dismiss();
                Toast.makeText(BannerManager.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            path = data.getData();
            btnSelect.setText("Image Selected!");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)){
            deleteBanner(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteBanner(String key) {
        banners.child(key).removeValue();
        Toast.makeText(BannerManager.this, "Delete Success", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(String key, Banner item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerManager.this);
        alertDialog.setTitle("Update Banner");
        alertDialog.setMessage("Please write the name of banner");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_banner = inflater.inflate(R.layout.add_new_banner,null);

        txtIdFood = add_banner.findViewById(R.id.editId);
        txtName = add_banner.findViewById(R.id.editName);
        btnSelect = add_banner.findViewById(R.id.btnSelect);
        btnUpload = add_banner.findViewById(R.id.btnUpload);

        //Set default value
        txtIdFood.getEditText().setText(item.getId());
        txtName.getEditText().setText(item.getName());

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

        alertDialog.setView(add_banner);
        alertDialog.setIcon(R.drawable.heart);

        //Set button
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Create new category
                if (newBanner != null){
                    item.setId(txtIdFood.getEditText().getText().toString());
                    item.setName(txtName.getEditText().getText().toString());

                    Map<String,Object> update = new HashMap<>();
                    update.put("id",item.getId());
                    update.put("name",item.getName());
                    update.put("image",item.getImage());

                    banners.child(key).updateChildren(update)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadListBanner();
                                    Snackbar.make(rootLayout,"New food " +newBanner.getName()+"was added.", Snackbar.LENGTH_SHORT).show();
                                    loadListBanner();
                                }
                            });
                }else
                    Snackbar.make(rootLayout,"Thêm hình ảnh rồi mới up", Snackbar.LENGTH_SHORT).show();
                    loadListBanner();

            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadListBanner();
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}