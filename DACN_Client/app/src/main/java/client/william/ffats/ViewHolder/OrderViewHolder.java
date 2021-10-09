package client.william.ffats.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtAddress;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderPhone = (TextView)itemView.findViewById(R.id.order_phone);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtAddress = (TextView)itemView.findViewById(R.id.order_address);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(), false);

    }
}
