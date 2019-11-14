package com.demo.tikpic.gallery;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.BitmapWorkerTask;
import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.ViewPager.ViewPagerFragment;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private static final String TAG = "myDataAdapter";
    private MainActivity hostActivity;
    // private List<MediaFile> imageUrlList;
    private DataManager dataManager;
    private List<String> imageUrlList;

    DataAdapter(MainActivity activity) {
        hostActivity = activity;
        // dataManager = DataManager.getInstance(hostActivity);
        // imageUrlList = dataManager.getShowcaseOrAlbumOrIndex(1, 0);
        imageUrlList = hostActivity.getImageUrlList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        /*
        Glide.with(hostActivity)
                .asBitmap()
                .load(imageUrlList.get(position))
                .into(holder.mImageView);
        */
        BitmapWorkerTask workerTask = new BitmapWorkerTask(hostActivity, holder.mImageView);


        Log.d(TAG, "onBindViewHolder: url: " + imageUrlList.get(position));

        workerTask.execute(imageUrlList.get(position));

    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ImageView mImageView;
        final DataAdapter mDataAdapter;

        ViewHolder(@NonNull View itemView, DataAdapter adapter) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.itemImageView);
            mDataAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            hostActivity.replaceFragment(new ViewPagerFragment());
        }
    }

}


