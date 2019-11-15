package com.demo.tikpic.gallery;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.BitmapWorkerTask;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.ViewPager.ViewPagerFragment;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private static final String TAG = "myDataAdapter";
    private MainActivity hostActivity;
    private List<String> imageUrlList;

    DataAdapter(MainActivity activity) {
        hostActivity = activity;
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

        BitmapWorkerTask workerTask = new BitmapWorkerTask(hostActivity, holder.mImageView);
        workerTask.execute(imageUrlList.get(position));
        Log.d(TAG, "onBindViewHolder: url: " + imageUrlList.get(position));
        Log.d(TAG, "onBindViewHolder: position: " + position);
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
                        implements View.OnClickListener {

        private ImageView mImageView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.itemImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            hostActivity.replaceFragment(new ViewPagerFragment());
        }
    }

}


