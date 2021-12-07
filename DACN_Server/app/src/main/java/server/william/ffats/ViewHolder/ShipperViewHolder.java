package server.william.ffats.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import server.william.ffats.Interface.ItemClickListener;
import server.william.ffats.R;

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtName,txtPhone,txtSumOrders;
    public ImageView imvAvata;
    public Button btnEdit,btnRemove;
    private ItemClickListener itemClickListener;

    public ShipperViewHolder(@NonNull View itemView) {
        super(itemView);

        txtName = itemView.findViewById(R.id.txtName);
        txtPhone = itemView.findViewById(R.id.txtPhone);
        txtSumOrders = itemView.findViewById(R.id.txtSumOrders);
        imvAvata = itemView.findViewById(R.id.imvAvata);
        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnRemove = itemView.findViewById(R.id.btnRemove);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
