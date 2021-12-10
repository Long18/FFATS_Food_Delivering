package shipper.william.ffats.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import shipper.william.ffats.R;

public class OrdersViewHolder extends RecyclerView.ViewHolder {

    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtAddress, txtDate;
    public Button btnShipping;


    public OrdersViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtAddress = itemView.findViewById(R.id.order_address);
        txtDate = itemView.findViewById(R.id.order_date);

        btnShipping = itemView.findViewById(R.id.btnStart);


    }

}