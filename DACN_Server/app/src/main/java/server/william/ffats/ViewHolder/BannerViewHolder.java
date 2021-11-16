package server.william.ffats.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import server.william.ffats.Common.Common;
import server.william.ffats.Interface.ItemClickListener;
import server.william.ffats.R;

public class BannerViewHolder extends RecyclerView.ViewHolder implements
        View.OnCreateContextMenuListener{

    public TextView txtBannerName;
    public ImageView imageBanner;

    private ItemClickListener itemClickListener;


    public BannerViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
    }

    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);

        txtBannerName = itemView.findViewById(R.id.banner_name);
        imageBanner = itemView.findViewById(R.id.banner_image);

        itemView.setOnCreateContextMenuListener(this);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}
