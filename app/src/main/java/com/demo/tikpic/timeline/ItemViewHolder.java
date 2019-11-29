package com.demo.tikpic.timeline;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.R;

public final class ItemViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    public final ImageView imageView;
    ImageView mVideoIconImageView;
    boolean loading = false;
    // final TextView tvItem;
    // final TextView tvSubItem;



    ItemViewHolder(@NonNull final View view) {
        super(view);

        rootView = view;
        imageView = view.findViewById(R.id.imgItem);
        mVideoIconImageView = itemView.findViewById(R.id.videoPlaybackIcon);
        // tvItem = view.findViewById(R.id.tvItem);
        // tvSubItem = view.findViewById(R.id.tvSubItem);


    }
    public boolean isLoading() {
        return loading;
    }
    public void switchLoadState() {
        loading = !loading;
    }

}
