package server.william.ffats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import server.william.ffats.Common.CircleTransform;
import server.william.ffats.Common.Common;
import server.william.ffats.Database.SessionManager;
import server.william.ffats.Model.Shipper;
import server.william.ffats.ViewHolder.ShipperViewHolder;

public class ShipperManagermentActivity extends AppCompatActivity {

    FloatingActionButton fabAdd;

    FirebaseDatabase database;
    DatabaseReference shippers;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    Uri saveUri;

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Shipper, ShipperViewHolder> adapter;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    String uriImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_managerment);

        insertData();
        createConstructor();
    }

    private void createConstructor() {
        recyclerView = findViewById(R.id.recyclerShippers);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShipperLayout();
            }
        });

        loadAllShippers();
    }


    private void insertData() {
        database = FirebaseDatabase.getInstance();
        shippers = database.getReference("Shippers");
        shippers.keepSynced(true);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

    }


    private void loadAllShippers() {
        FirebaseRecyclerOptions<Shipper> options = new FirebaseRecyclerOptions.Builder<Shipper>()
                .setQuery(shippers, Shipper.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ShipperViewHolder holder,@SuppressLint("RecyclerView") int position, @NonNull Shipper model) {
                holder.txtName.setText(model.getName());
                holder.txtPhone.setText(model.getPhone());
                holder.txtSumOrders.setText(model.getSumOrders());

                Picasso.get().load(model.getImage()).transform(new CircleTransform())
                        .into(holder.imvAvata);

                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(adapter.getRef(position).getKey(),model);
                    }
                });

                holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeShipper(adapter.getRef(position).getKey());
                    }
                });
            }

            @NonNull
            @Override
            public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shipper_layout, parent, false);
                return new ShipperViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void removeShipper(String key) {
        shippers.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ShipperManagermentActivity.this, "Remove Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShipperManagermentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void showEditDialog(String key,Shipper model) {
        AlertDialog.Builder create_shipper = new AlertDialog.Builder(ShipperManagermentActivity.this);
        create_shipper.setTitle("Update Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_shipper_layout, null);

        TextInputLayout tilName = view.findViewById(R.id.txtLayoutName);
        TextInputLayout tilPhone = view.findViewById(R.id.txtLayoutPhone);
        TextInputLayout tilPass = view.findViewById(R.id.txtLayoutPassword);
        ImageView imvProfive = view.findViewById(R.id.imvProfile);

        tilPhone.getEditText().setEnabled(false);

        //set data
        tilName.getEditText().setText(model.getName());
        tilPhone.getEditText().setText(model.getPhone());
        tilPass.getEditText().setText(model.getPassword());

        Picasso.get().load(model.getImage()).transform(new CircleTransform())
                .into(imvProfive);

        imvProfive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();

                Picasso.get().load(uriImage).transform(new CircleTransform())
                        .into(imvProfive);
            }
        });

        create_shipper.setView(view);
        create_shipper.setIcon(R.drawable.add_shipper);
        create_shipper.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Map<String,Object> update = new HashMap<>();
                update.put("image",model.getImage());
                update.put("name",tilName.getEditText().getText().toString());
                update.put("password",tilPass.getEditText().getText().toString());
                update.put("phone",tilPhone.getEditText().getText().toString());

                shippers.child(key)
                        .updateChildren(update)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ShipperManagermentActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagermentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        create_shipper.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        create_shipper.show();

    }

    private void showShipperLayout() {
        AlertDialog.Builder create_shipper = new AlertDialog.Builder(ShipperManagermentActivity.this);
        create_shipper.setTitle("Create Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_shipper_layout, null);

        TextInputLayout tilName = view.findViewById(R.id.txtLayoutName);
        TextInputLayout tilPhone = view.findViewById(R.id.txtLayoutPhone);
        TextInputLayout tilPass = view.findViewById(R.id.txtLayoutPassword);
        ImageView imvProfive = view.findViewById(R.id.imvProfile);


        imvProfive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();

                Picasso.get().load(uriImage).transform(new CircleTransform())
                        .into(imvProfive);
            }
        });



        create_shipper.setView(view);
        create_shipper.setIcon(R.drawable.add_shipper);
        create_shipper.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Shipper shipper = new Shipper();
                shipper.setName(tilName.getEditText().getText().toString());
                shipper.setPhone(tilPhone.getEditText().getText().toString());
                shipper.setPassword(tilPass.getEditText().getText().toString());
                shipper.setSumOrders(null);
                shipper.setImage(uriImage);

                shippers.child(tilPhone.getEditText().getText().toString())
                        .setValue(shipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ShipperManagermentActivity.this, "Create Success", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagermentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        create_shipper.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        create_shipper.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Select Image"), Common.CHOOSE_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (saveUri != null) {
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("shippers/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ShipperManagermentActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set value for new user if image upload
                                    uriImage = uri.toString();

                                }
                            });
                        }
                    }).addOnFailureListener(e -> {
                mDialog.dismiss();
                Toast.makeText(ShipperManagermentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                && data != null && data.getData() != null) {
            saveUri = data.getData();
            uploadImage();
            Toast.makeText(ShipperManagermentActivity.this, "Added Image Success!", Toast.LENGTH_SHORT).show();

        }
    }
}