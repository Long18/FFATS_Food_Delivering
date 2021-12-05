package client.william.ffats.Account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.UUID;

import client.william.ffats.Common.CircleTransform;
import client.william.ffats.Common.Common;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Model.User;
import client.william.ffats.R;
import io.paperdb.Paper;

public class Information extends AppCompatActivity {

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    TextView txtName, txtAdress;
    TextInputLayout tilName, tilEmail, tilNumberPhone;
    ImageView btnProfilePicture, btnSave;

    Uri saveUri;

    FirebaseDatabase database;
    DatabaseReference user;

    FirebaseStorage storage;
    StorageReference storageReference;

    User userInfo;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_avata) {
                chooseImage();
            }
            if (v.getId() == R.id.ic_save) {

                if (btnSave.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.check_circle).getConstantState()){
                    btnSave.setImageResource(R.drawable.check_circle_enable);
                    tilName.getEditText().setEnabled(true);
                    tilEmail.getEditText().setEnabled(true);
                    tilNumberPhone.getEditText().setEnabled(true);

                    tilName.getEditText().requestFocus();
                }else if (btnSave.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.check_circle_enable).getConstantState()){
                    btnSave.setImageResource(R.drawable.check_circle);
                    tilName.getEditText().setEnabled(false);
                    tilEmail.getEditText().setEnabled(false);
                    tilNumberPhone.getEditText().setEnabled(false);
                    changeInformation();
                }
            }
        }
    };


    //region Function Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        insertData();
        viewConstructor();
    }

    private void insertData() {
        Paper.init(this);
        database = FirebaseDatabase.getInstance();
        user = database.getReference("user");
        user.keepSynced(true);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void viewConstructor() {
        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        txtName = findViewById(R.id.txtFullName);
        txtAdress = findViewById(R.id.txtAddress);
        tilName = findViewById(R.id.txtName);
        tilEmail = findViewById(R.id.txtEmail);
        tilNumberPhone = findViewById(R.id.txtPhoneNumner);
        btnProfilePicture = findViewById(R.id.img_avata);
        btnSave = findViewById(R.id.ic_save);

        btnProfilePicture.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);

        txtName.setText(userInformation.get(SessionManager.KEY_FULLNAME));
        txtAdress.setText("Address: " + userInformation.get(SessionManager.KEY_ADDRESS));
        tilName.getEditText().setText(userInformation.get(SessionManager.KEY_FULLNAME));
        tilEmail.getEditText().setText(userInformation.get(SessionManager.KEY_EMAIL));
        tilNumberPhone.getEditText().setText(userInformation.get(SessionManager.KEY_PHONENUMBER));

        showInformation();
    }


    //endregion

    //region Function

    private void changeInformation() {

        Query getUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone");


        getUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String mName = tilName.getEditText().getText().toString();
                String mEmail = tilEmail.getEditText().getText().toString();
                String mPhoneNo = tilNumberPhone.getEditText().getText().toString();
                String mAddress = userInformation.get(SessionManager.KEY_ADDRESS);

                user.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("name").setValue(mName);
                user.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("email").setValue(mEmail);
                user.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("phone").setValue(mPhoneNo);
                user.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("address").setValue(mAddress);

                sessionManager.createEmail(snapshot.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("email").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        showInformation();
        Toast.makeText(this, "Changed success!!", Toast.LENGTH_SHORT).show();
    }

    private void showInformation() {

        Query getUser = FirebaseDatabase.getInstance().getReference("user").orderByChild("phone");

        if (!(userInformation.get(SessionManager.KEY_IMAGE) == null)) {
            getUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String Image = snapshot.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("image").getValue(String.class);
                    Picasso.get().load(Image).transform(new CircleTransform())
                            .into(btnProfilePicture);

                    sessionManager.createEmail(snapshot.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("email").getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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
            StorageReference imageFolder = storageReference.child("profiles/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Information.this, "Success!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set value for new user if image upload
                                    user.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).child("image").setValue(uri.toString());
                                    sessionManager.createImage(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(e -> {
                mDialog.dismiss();
                Toast.makeText(Information.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            mDialog.setMessage("Upload Success" + progress + "%");
                            showInformation();
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
            Toast.makeText(Information.this, "Changed Image Success!", Toast.LENGTH_SHORT).show();

        }
    }}