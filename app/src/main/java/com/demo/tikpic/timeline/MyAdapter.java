package com.demo.tikpic.timeline;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.timeline.ItemViewHolder;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

class MyAdapter extends SectionedRecyclerViewAdapter {

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder instanceof ItemViewHolder){
            ItemViewHolder itemViewHolder = (ItemViewHolder)holder;

            if(itemViewHolder.isLoading()) {
                itemViewHolder.switchLoadState();
                //Log.d(TAG, "onViewRecycled: loading state switched");
                ((AsyncTask) itemViewHolder.imageView.getTag()).cancel(true);
            }
        }

    }
}