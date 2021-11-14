package client.william.ffats.ViewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import client.william.ffats.Cart;
import client.william.ffats.Common.Common;
import client.william.ffats.Database.Database;
import client.william.ffats.Interface.ItemClickListener;
import client.william.ffats.Model.Order;
import client.william.ffats.R;

class CartViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener{

    public TextView txtCartName, txtPrice;
    public ElegantNumberButton btnQuantity;
    public ImageView cartImage;

    private ItemClickListener itemClickListener;

    public void setTxtCartName(TextView txtCartName) {
        this.txtCartName = txtCartName;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        txtCartName = itemView.findViewById(R.id.cart_item_name);
        txtPrice = itemView.findViewById(R.id.cart_item_Price);
        btnQuantity = itemView.findViewById(R.id.btn_quantity);
        cartImage = itemView.findViewById(R.id.cartImage);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);
    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View view = inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") final int position) {
//        TextDrawable drawable = TextDrawable.builder()
//                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
//        holder.imgCartCount.setImageDrawable(drawable);

        Picasso.get().load(listData.get(position).getImage())
                .resize(70,70)
                .centerCrop()
                .into(holder.cartImage);

        holder.btnQuantity.setNumber(listData.get(position).getQuantity());
        holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //calculating total price
                int total = 0;
                List<Order> orders = new Database(cart).getCart();
                for (Order item : orders)
                    total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
                Locale locale = new Locale("vn", "VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                cart.txtTotalPrice.setText(fmt.format(total));
            }
        });

        Locale locale = new Locale("vn","VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer
                .parseInt(listData
                        .get(position)
                        .getPrice())) * (Integer
                .parseInt(listData
                        .get(position)
                        .getQuantity()
                ));
        holder.txtPrice.setText(fmt.format(price));
        holder.txtCartName.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
