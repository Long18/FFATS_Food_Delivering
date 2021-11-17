package client.william.ffats.ViewHolder;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import client.william.ffats.Database.Database;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.FoodDetail;
import client.william.ffats.FoodList;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Model.Favorites;
import client.william.ffats.Model.Food;
import client.william.ffats.Model.Order;
import client.william.ffats.R;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    private Context context;
    private List<Favorites> favList;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    public FavoritesAdapter(Context context, List<Favorites> favList) {
        this.context = context;
        this.favList = favList;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.favorites_item,parent,false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txtFoodName.setText(favList.get(position).getFoodName());
        holder.txtFoodPrice.setText(String.format("%s đ",favList.get(position).getFoodPrice().toString()));

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        Picasso.get().load(favList.get(position).getFoodImage())
                .into(holder.imageFood);

        boolean isExists = new Database(context)
                .checkFoodExists(favList.get(position).getFoodId(),userInformation.get(SessionManager.KEY_PHONENUMBER));
        holder.btnQuickCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExists) {
                    new Database(context).addToCart(new Order(
                            userInformation.get(SessionManager.KEY_PHONENUMBER),
                            favList.get(position).getFoodId(),
                            favList.get(position).getFoodName(),
                            "1",
                            favList.get(position).getFoodPrice(),
                            favList.get(position).getFoodDiscount(),
                            favList.get(position).getFoodImage()
                    ));
                }else {
                    new Database(context)
                            .increaseCart(userInformation.get(SessionManager.KEY_PHONENUMBER),favList.get(position).getFoodId());
                }
                Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });



        final Favorites local = favList.get(position);
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //Start new Activity
                Intent foodDetail = new Intent(context, FoodDetail.class);
                //Send Id to new activity
                foodDetail.putExtra("FoodId",favList.get(position).getFoodId());
                context.startActivity(foodDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favList.size();
    }

    public void removeItem(int position){
        favList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item,int position){
        favList.add(position,item);
        notifyItemInserted(position);
    }

    public Favorites getItem(int position){
        return favList.get(position);
    }
}
