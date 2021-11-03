package client.william.ffats.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import client.william.ffats.Common.Common;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{

    public TextView txtMenuName,txtAddress;
    public ImageView imageView;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        txtMenuName = itemView.findViewById(R.id.menu_name);
        txtAddress = itemView.findViewById(R.id.menu_address);
        imageView = itemView.findViewById(R.id.menu_image);


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
