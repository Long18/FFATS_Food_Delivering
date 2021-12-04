package client.william.ffats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import client.william.ffats.Database.Database;
import client.william.ffats.Database.SessionManager;
import client.william.ffats.Helper.RecyclerItemTouchHelper;
import client.william.ffats.Interface.RecyclerItemTouchListener;
import client.william.ffats.Model.Favorites;
import client.william.ffats.Model.Order;
import client.william.ffats.ViewHolder.CartAdapter;
import client.william.ffats.ViewHolder.CartViewHolder;
import client.william.ffats.ViewHolder.FavoritesAdapter;
import client.william.ffats.ViewHolder.FavoritesViewHolder;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchListener {
    //region Declare Variable
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    TextView txtNull;

    FirebaseDatabase database;
    DatabaseReference foodList;

    SessionManager sessionManager;
    HashMap<String, String> userInformation;

    RelativeLayout rootLayout;

    FavoritesAdapter adapter;
    //endregion


    //region Activity Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        viewConstructor();
    }

    private void viewConstructor() {
        rootLayout = findViewById(R.id.root_layout_fav);

        recyclerView = findViewById(R.id.recycler_favorites);
        txtNull = findViewById(R.id.txtNull);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
        userInformation = sessionManager.getInfomationUser();

        //Swipe delete
        ItemTouchHelper.SimpleCallback iSimpleCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,FavoritesActivity.this);
        new ItemTouchHelper(iSimpleCallback).attachToRecyclerView(recyclerView);

        loadFavorites();
    }

    //endregion

    //region Function
    private void loadFavorites() {
        int count = new Database(this).getCountFavorites(userInformation.get(SessionManager.KEY_PHONENUMBER));
        if (count == 1){
            txtNull.setVisibility(View.INVISIBLE);
        }else {
            txtNull.setVisibility(View.VISIBLE);
        }

        adapter = new FavoritesAdapter(this, new Database(this).getFavorites(userInformation.get(SessionManager.KEY_PHONENUMBER)));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder){
            String name = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();

            Favorites deleteItem = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFavorites(deleteItem.getFoodId(),userInformation.get(SessionManager.KEY_PHONENUMBER));

            //Popup Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name + "removed from favorite",Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem);
                    loadFavorites();
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            loadFavorites();
        }
    }
    //endregion
}