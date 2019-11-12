package com.demo.tikpic.timeline;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.R;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView tvTitle;
    final Button btnMore;

    HeaderViewHolder(@NonNull final View view) {
        super(view);

        tvTitle = view.findViewById(R.id.tvTitle);
        btnMore = view.findViewById(R.id.btnMore);
    }
}
