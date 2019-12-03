package com.demo.tikpic.albums;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.gallery.GalleryFragment;
import com.demo.tikpic.itemClass.MediaAlbum;

import java.util.List;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private MainActivity hostActivity;
    private List<MediaAlbum> showCaseList;
    private ClickListener mClickListener;
    private int albumCoverWidth;

    private static final int CASE_ALBUM = 3;
    private static final String TAG = "AlbumsAdapter";

    AlbumsAdapter(MainActivity activity, List<MediaAlbum> list, int coverWidth, final ClickListener clickListener) {
        hostActivity = activity;
        showCaseList = list;
        albumCoverWidth = coverWidth;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_cover_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.albumName.setText(showCaseList.get(position).getName());
        Log.d(TAG, "onBindViewHolder: " + showCaseList.get(position).getName());
        Log.d(TAG, "onBindViewHolder: " + showCaseList.get(position).getAlbumSize());
        int count = showCaseList.get(position).getAlbumSize();
        String countString = Integer.toString(count);
        holder.albumPictureCount.setText(countString);
        DataManager.getInstance(hostActivity).loadBitmap(showCaseList.get(position).get(0), holder, CASE_ALBUM);

        holder.rootView.setOnClickListener(v ->
                mClickListener.onAlbumViewClicked(position)
        );
    }

    @Override
    public int getItemCount() {
        return showCaseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View rootView;
        public ImageView albumCoverImage;
        private TextView albumName;
        private TextView albumPictureCount;

        public int getCoverSize() {
            return coverSize;
        }

        private int coverSize;

        private boolean loading = false;
        private LinearLayout.LayoutParams mLayoutParams;

        ViewHolder(View view) {
            super(view);
            rootView = view;
            coverSize = albumCoverWidth;
            LinearLayout layout = itemView.findViewById(R.id.coverLayout);
            albumCoverImage = itemView.findViewById(R.id.albumCoverView);
            albumName = itemView.findViewById(R.id.albumName);
            albumPictureCount = itemView.findViewById(R.id.albumPictureAmount);
            mLayoutParams = new LinearLayout.LayoutParams(albumCoverWidth, albumCoverWidth);
            layout.setLayoutParams(mLayoutParams);

        }

        public boolean isLoading() {
            return loading;
        }

        public void switchLoadState() {
            loading = !loading;
        }
    }


    interface ClickListener {

        void onAlbumViewClicked(final int albumNumber);
    }
}
