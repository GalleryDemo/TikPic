package com.demo.tikpic.gallery;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
    GridLayoutManager gridLayoutManager;
    private int firstPosition,lastPosition;
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
            Log.d(TAG, "onCreate - BUNDLE: " + albumNumber + " LIFECYCLE");
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
        recyclerView.setItemViewCacheSize(5);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        gridLayoutManager = new GridLayoutManager(hostActivity, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                if(lm != null && lm instanceof GridLayoutManager){
                    firstPosition = ((GridLayoutManager)lm).findFirstVisibleItemPosition();
                    lastPosition = ((GridLayoutManager)lm).findLastVisibleItemPosition();
                    int count = lm.getChildCount();
                    if(firstPosition > DataManager.getInstance().getCurrenPosition() || lastPosition < DataManager.getInstance().getCurrenPosition()){
                        gridLayoutManager.scrollToPosition(DataManager.getInstance().getCurrenPosition());
                    }
                }
            }
        },100);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0 || dy < 0){
                    firstPosition = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    lastPosition = gridLayoutManager.findLastCompletelyVisibleItemPosition();
                    Log.d(TAG, "onScrolled:  first: " + firstPosition);
                    Log.d(TAG, "onScrolled:  last: " + lastPosition);

                }
            }
        });

        Log.d(TAG, "onCreateView: LIFECYCLE ==");



        return root;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: LIFECYCLE");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: LIFECYCLE");
    }

    @Override
    public void onResume() {
        super.onResume();

//        Log.d(TAG, "onResume: FIRST: " + firstPosition);
//        Log.d(TAG, "onResume: CURRENT: " + DataManager.getInstance().getCurrenPosition());
//        Log.d(TAG, "onResume: LAST: " + lastPosition);
//        if(firstPosition > DataManager.getInstance().getCurrenPosition() || lastPosition < DataManager.getInstance().getCurrenPosition()){
//            gridLayoutManager.scrollToPosition(DataManager.getInstance().getCurrenPosition());
//        }





        Log.d(TAG, "onResume: LIFECYCLE");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: LIFECYCLE");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: LIFECYCLE");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: LIFECYCLE");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: LIFECYCLE");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: LIFECYCLE");
    }

    @Override
    public void onClick(int position) {
        //DataManager.getInstance().setCurrenPosition(-1);
        if(isFromAlbumView){
            hostActivity.photoPage(0,albumNumber,position);
            Log.d(TAG, "onClick - albumNumber: " + albumNumber);
        }else{
            hostActivity.photoPage(1,0,position);
            Log.d(TAG, "onClick - albumNumber: " + position);
        }

    }
}