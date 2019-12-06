package com.demo.tikpic.gallery;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.ViewPagerActivity;
import com.demo.tikpic.itemClass.MediaAlbum;
import com.demo.tikpic.itemClass.MediaFile;

import java.util.List;

public class GalleryFragment extends Fragment implements DataAdapter.ClickListener{

    private static final String TAG = "GalleryFragment";
    private static final String ALBUM_NAME = "albumName";
    private MediaAlbum currentAlbum;
    private MainActivity hostActivity;
    private int albumNumber;
    private boolean isFromAlbumView;
    private int mScreenWidth;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: " + outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        Bundle arguments = getArguments();

        super.onCreate(savedInstanceState);
        MainActivity hostActivity = (MainActivity) getActivity();
        if(arguments != null){
            isFromAlbumView = true;
            albumNumber = arguments.getInt(ALBUM_NAME,0);
            Log.d(TAG, "onCreate - BUNDLE: " + albumNumber);
            currentAlbum = DataManager.getInstance(hostActivity).getShowcaseOrAlbumOrIndex(0).get(albumNumber);
            Log.d(TAG, "onCreate - ALBUM SIZE: " + currentAlbum.getAlbumSize());
        }else{
            isFromAlbumView = false;
            Log.d(TAG, "onCreate - BUNDLE: NULL");
            currentAlbum = DataManager.getInstance(hostActivity).getShowcaseOrAlbumOrIndex(1).get(0);
        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        hostActivity = (MainActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mScreenWidth = metrics.widthPixels;

        int itemWidth = mScreenWidth/4;


        DataAdapter dataAdapter = new DataAdapter(hostActivity, currentAlbum,this,itemWidth);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(dataAdapter);

        //recyclerView.setItemViewCacheSize(30);
        recyclerView.hasFixedSize();
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setItemViewCacheSize(60);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(hostActivity, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        return root;
    }

    @Override
    public void onClick(int position) {

        if(isFromAlbumView){
            hostActivity.photoPage(0,albumNumber,position);
            Log.d(TAG, "onClick - albumNumber: " + albumNumber);
        }else{
            hostActivity.photoPage(1,0,position);
            Log.d(TAG, "onClick - albumNumber: " + position);
        }

    }
}