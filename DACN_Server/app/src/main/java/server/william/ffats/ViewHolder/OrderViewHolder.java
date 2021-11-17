package server.william.ffats.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import server.william.ffats.Common.Common;
import server.william.ffats.Interface.ItemClickListener;
import server.william.ffats.R;


public class OrderViewHolder extends RecyclerView.ViewHolder
{

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtAddress,txtDate;
    public Button btnEdit,btnRemove,btnDetail,btnDirection;


    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtAddress = itemView.findViewById(R.id.order_address);
        txtDate = itemView.findViewById(R.id.order_date);

        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnRemove = itemView.findViewById(R.id.btnRemove);
        btnDetail = itemView.findViewById(R.id.btnDetail);
        btnDirection = itemView.findViewById(R.id.btnDirection);


    }


}
