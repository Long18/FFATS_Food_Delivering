package client.william.ffats.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import client.william.ffats.Database.SessionManager;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtFoodName,txtFoodPrice;
    public ImageView imageFood,btnQuickCart;

    private ItemClickListener itemClickListener;

    public RelativeLayout background;
    public LinearLayout foreground;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FavoritesViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
    }

    public FavoritesViewHolder(@NonNull View itemView) {
        super(itemView);

        txtFoodName = itemView.findViewById(R.id.food_name_fav);
        txtFoodPrice = itemView.findViewById(R.id.food_price_fav);
        imageFood = itemView.findViewById(R.id.food_image_fav);
        btnQuickCart = itemView.findViewById(R.id.btn_quick_cart_fav);
        background = itemView.findViewById(R.id.view_background);
        foreground = itemView.findViewById(R.id.view_foreground);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
