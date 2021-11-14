package client.william.ffats.ViewHolder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import client.william.ffats.R;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtPhone,txtComment;
    public RatingBar ratingBar;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        txtPhone = itemView.findViewById(R.id.txtPhoneNumner);
        txtComment = itemView.findViewById(R.id.txtComment);
        ratingBar = itemView.findViewById(R.id.ratingBar);
    }
}
