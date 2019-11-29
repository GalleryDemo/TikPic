package com.demo.tikpic.timeline;

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
    private final List<String> list;
    private final MainActivity hostActivity;
    private final ClickListener clickListener;
    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_VIDEO = 1;

    PhotoSection(@NonNull final String title, @NonNull final List<String> list,
                 @NonNull final MainActivity activity, @NonNull final ClickListener clickListener) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.photo_section_item)
                .headerResourceId(R.layout.photo_section_header)
                .build());

        this.title = title;
        this.list = list;
        this.hostActivity = activity;
        this.clickListener = clickListener;
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

        final String photo = list.get(position);

        Glide.with(hostActivity)
                .asBitmap()
                .load(photo)
                .into(itemHolder.imageView);

//        if(allFiles.get(position).getType() == 3) {
//            holder.mVideoIconImageView.setVisibility(View.VISIBLE);
//            DataManager.getInstance(hostActivity).loadBitmap(position, holder, TYPE_VIDEO);
//        }
//        else {
//            holder.mVideoIconImageView.setVisibility(View.GONE);
//            DataManager.getInstance(hostActivity).loadBitmap(position, holder, TYPE_IMAGE);
//        }

        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(title, itemHolder.getAdapterPosition())
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
