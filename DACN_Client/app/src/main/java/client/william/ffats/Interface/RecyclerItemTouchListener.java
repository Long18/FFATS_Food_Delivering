package client.william.ffats.Interface;

import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerItemTouchListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder,int direction,int position);
}
