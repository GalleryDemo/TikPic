package com.demo.tikpic.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.ViewPagerFragment;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private MainActivity hostActivity;
    private List<String> imageUrlList;

    DataAdapter(MainActivity activity, List<String> UrlList) {
        hostActivity = activity;
        imageUrlList = UrlList;
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

        Glide.with(hostActivity)
                .asBitmap()
                .load(imageUrlList.get(position))
                .into(holder.mImageView);
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
            FragmentManager fragmentManager = hostActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mainLayout_FrameLayout, new ViewPagerFragment());
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
        }
    }

}
