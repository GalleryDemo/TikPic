package com.oppo.tikpic.timeline;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oppo.tikpic.R;

final class ItemViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    final ImageView imageView;
    // final TextView tvItem;
    // final TextView tvSubItem;

    ItemViewHolder(@NonNull final View view) {
        super(view);

        rootView = view;
        imageView = view.findViewById(R.id.imgItem);
        // tvItem = view.findViewById(R.id.tvItem);
        // tvSubItem = view.findViewById(R.id.tvSubItem);
    }
}
