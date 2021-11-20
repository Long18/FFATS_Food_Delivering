package client.william.ffats.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.R;

public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{

    public TextView txtRes;
    public ImageView imageView;

    private ItemClickListener itemClickListener;

    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);

        txtRes = itemView.findViewById(R.id.res_name);
        imageView = itemView.findViewById(R.id.res_image);


        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }


}

