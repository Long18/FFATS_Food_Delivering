package client.william.ffats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;
import java.util.HashMap;

import client.william.ffats.Common.Common;
import client.william.ffats.Database.Database;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Model.Food;
import client.william.ffats.Model.Order;
import client.william.ffats.Model.Rate;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {
    //region Declare Variable
    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRate;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;
    Button btnShowComment;

    String foodId="";

    FirebaseDatabase database;
    DatabaseReference foods, ratingDB;

    Food currentFood;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //region Button Cart
            if (v.getId() == R.id.btnCart){
                new Database(getBaseContext()).addToCart(new Order(
                        userInformation.get(SessionManager.KEY_PHONENUMBER),
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ));

                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
            //endregion
            //region Button Rate
            if (v.getId() == R.id.btnRate){
                showRatingDialog();
            }
            //endregion
            //region Show Comment
            if (v.getId() == R.id.btn_ShowComment){
                Intent intent = new Intent(FoodDetail.this,Comment.class);
                intent.putExtra("FoodId",foodId);
                startActivity(intent);
            }
            //endregion
        }
    };
    //endregion

    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        viewConstructor();

    }

    private void viewConstructor() {
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingDB = database.getReference("Rating");

        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnRate = findViewById(R.id.btnRate);
        btnShowComment = findViewById(R.id.btn_ShowComment);
        ratingBar = findViewById(R.id.rateBar);

        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);
        food_description = findViewById(R.id.food_description);
        food_image = findViewById(R.id.img_food);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        //collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        //collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        btnCart.setOnClickListener(onClickListener);
        btnRate.setOnClickListener(onClickListener);
        btnShowComment.setOnClickListener(onClickListener);

        //Get FoodId
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()){
            if (Common.isConnectedToInternet(getBaseContext())){
                getDetailFood(foodId);
                getRatingFood(foodId);
            }
            else {
                Toast.makeText(FoodDetail.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    //endregion

    //region Function
    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                currentFood = snapshot.getValue(Food.class);

                Picasso.get().load(currentFood.getImage())
                        .into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(String.format("%s Đ",currentFood.getPrice().toString()));
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays
                        .asList("Very Bad","Not Good","Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some starts and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(R.color.black)
                .setCommentBackgroundColor(R.color.colorPrimaryButton)
                .setWindowAnimation(R.style.RatingDialogFadeAnimation)
                .create(FoodDetail.this)
                .show();
    }

    @Override
    public void onNegativeButtonClicked() {
    }

    @Override
    public void onNeutralButtonClicked() {
    }

    @Override
    public void onPositiveButtonClicked(int value, @NonNull String comments) {
        //Get Rating and upload
        SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        HashMap<String, String> userInformation = sessionManager.getInfomationUser();

        Rate rating = new Rate(userInformation.get(SessionManager.KEY_PHONENUMBER),
                userInformation.get(SessionManager.KEY_FULLNAME),
                foodId,
                String.valueOf(value),
                comments);
                ratingDB.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Thanks for submit rating food", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingDB.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot PostSnapshot:snapshot.getChildren()){
                    Rate item = PostSnapshot.getValue(Rate.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0 ){
                    float average = sum/count;
                    ratingBar.setRating(average);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCart.setCount(new Database(FoodDetail.this).getCountCart(userInformation.get(SessionManager.KEY_PHONENUMBER)));
    }

    //endregion
}