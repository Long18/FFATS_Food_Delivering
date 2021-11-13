package client.william.ffats.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtFoodName,txtFoodPrice;
    public ImageView imageFood,fav_image,fav_share,btnQuickCart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        txtFoodName = itemView.findViewById(R.id.food_name);
        txtFoodPrice = itemView.findViewById(R.id.food_price);
        imageFood = itemView.findViewById(R.id.food_image);
        fav_image = itemView.findViewById(R.id.fav_image);
        fav_share = itemView.findViewById(R.id.fav_share);
        btnQuickCart = itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
