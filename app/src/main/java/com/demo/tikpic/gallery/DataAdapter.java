package com.demo.tikpic.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;

import com.demo.tikpic.itemClass.MediaAlbum;




public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private static final String TAG = "MYSYNC";

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_VIDEO = 1;

    private MainActivity hostActivity;
    private MediaAlbum currentAlbum;
    private ClickListener mClickListener;

    DataAdapter(MainActivity activity,MediaAlbum album, ClickListener listener) {
        hostActivity = activity;
        currentAlbum = album;
        mClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // load placeholder
        holder.mImageView.setImageDrawable(
                hostActivity.getDrawable(R.drawable.ic_launcher_foreground));

        int index = currentAlbum.get(position);

        // load media thumbnail
        if(DataManager.getInstance(hostActivity).getAllItemList().get(index).getType() == 3) {
            holder.mVideoIconImageView.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder - holder  : " + holder.getAdapterPosition());
            DataManager.getInstance(hostActivity).loadBitmap(index, holder, TYPE_VIDEO);
        }
        else {
            holder.mVideoIconImageView.setVisibility(View.GONE);
           // DataManager.getInstance(hostActivity).loadBitmap(index, holder, TYPE_IMAGE);
            Glide.with(holder.rootView).load(DataManager.getInstance().getShowcaseOrAlbumOrIndex(1,0,position).getPath()).into(holder.mImageView);
        }

        holder.rootView.setOnClickListener(v ->
                mClickListener.onClick(index)
                );


    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

        if(holder.isLoading()) {
            holder.switchLoadState();
            Log.d(TAG, "onViewRecycled: loading state switched");
            ((AsyncTask) holder.mImageView.getTag()).cancel(true);
        }
    }

    @Override
    public int getItemCount() {
        return currentAlbum.getAlbumSize();
    }

    // ==================== ViewHolder ==================== //

    public class ViewHolder extends RecyclerView.ViewHolder{

        final View rootView;
        public ImageView mImageView;
        private ImageView mVideoIconImageView;
        private boolean loading = false;

        public boolean isLoading() {
            return loading;
        }
        public void switchLoadState() {
            loading = !loading;
        }

        ViewHolder(@NonNull View view) {
            super(view);
            rootView = view;
            mImageView = itemView.findViewById(R.id.itemImageView);
            mVideoIconImageView = itemView.findViewById(R.id.videoPlaybackIcon);
        }

    }

    interface ClickListener{
        void onClick(final int position);
    }


}


