package com.demo.tikpic.gallery;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
            int albumNumber = arguments.getInt(ALBUM_NAME,0);
            Log.d(TAG, "onCreate - BUNDLE: " + albumNumber);
            currentAlbum = DataManager.getInstance(hostActivity).getShowcaseOrAlbumOrIndex(0).get(albumNumber);
        }else{
            currentAlbum = DataManager.getInstance(hostActivity).getShowcaseOrAlbumOrIndex(1).get(0);
        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        hostActivity = (MainActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        DataAdapter dataAdapter = new DataAdapter(hostActivity, currentAlbum,this);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(dataAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(hostActivity, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        return root;
    }

    @Override
    public void onClick(int position) {

        Intent intent = new Intent(hostActivity, ViewPagerActivity.class);
        intent.putExtra("position", position);
        Log.d("DEBUGALL", "onClick - dataAdapter - getAdapterPosition:  "+ position);
        hostActivity.startActivity(intent);
    }
}