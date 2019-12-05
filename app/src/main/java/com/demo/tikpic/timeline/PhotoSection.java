package com.demo.tikpic.timeline;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.gallery.DataAdapter;
import com.demo.tikpic.itemClass.MediaFile;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;




final class PhotoSection extends Section {

    private final String title;
    private final List<Integer> list;
    private final MainActivity hostActivity;
    private final ClickListener clickListener;
    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_VIDEO = 1;
    private static final String TAG = "PhotoSection";
    private int width;

    PhotoSection(@NonNull final String title, @NonNull final List<Integer> list,
                 @NonNull final MainActivity activity, @NonNull final ClickListener clickListener, int width) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.photo_section_item)
                .headerResourceId(R.layout.photo_section_header)
                .build());

        this.title = title;
        this.list = list;
        this.hostActivity = activity;
        this.clickListener = clickListener;
        this.width = width;
    }

    @Override
    public int getContentItemsTotal() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        final int positionInAll = list.get(position);
        Log.d("DEBUGALL", "onBindItemViewHolder position: " + position);


        itemHolder.imageView.setImageDrawable(
                hostActivity.getDrawable(R.drawable.ic_launcher_foreground));

        if(DataManager.getInstance(hostActivity).getAllItemList().get(positionInAll).getType() == 3) {
            //Log.d("DEBUGALL", "onBindItemViewHolder: " + positionInAll);
            itemHolder.mVideoIconImageView.setVisibility(View.VISIBLE);
            DataManager.getInstance(hostActivity).loadBitmap(positionInAll, itemHolder, width);
        }
        else {
            itemHolder.mVideoIconImageView.setVisibility(View.GONE);
            DataManager.getInstance(hostActivity).loadBitmap(positionInAll, itemHolder, width);
        }


        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(title, positionInAll)
        );

    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.tvTitle.setText(title);
        headerHolder.btnMore.setOnClickListener(v ->
                clickListener.onHeaderRootViewClicked(title, this)
        );
    }

    interface ClickListener {

        void onHeaderRootViewClicked(@NonNull final String sectionTitle, @NonNull final PhotoSection section);

        void onItemRootViewClicked(@NonNull final String sectionTitle, final int itemAdapterPosition);
    }
}
