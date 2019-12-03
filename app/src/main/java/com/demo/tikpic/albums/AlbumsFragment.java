package com.demo.tikpic.albums;

import android.content.Context;
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
import com.demo.tikpic.gallery.DataAdapter;
import com.demo.tikpic.gallery.GalleryFragment;

public class AlbumsFragment extends Fragment implements AlbumsAdapter.ClickListener{
    private static final String TAG = "AlbumsFragment";
    private static final String ALBUM_NAME = "albumName";
    MainActivity hostActivity;
    private int mScreenWidth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        hostActivity = (MainActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mScreenWidth = metrics.widthPixels;

        int itemWidth = mScreenWidth/2;

        AlbumsAdapter albumsAdapter = new AlbumsAdapter(hostActivity,
                DataManager.getInstance(hostActivity).getShowcaseOrAlbumOrIndex(0),
                itemWidth,
                this);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(albumsAdapter);



        GridLayoutManager gridLayoutManager = new GridLayoutManager(hostActivity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        return root;

    }

    @Override
    public void onAlbumViewClicked(int albumNumber) {
        GalleryFragment galleryFragment = new GalleryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ALBUM_NAME,albumNumber);
        galleryFragment.setArguments(bundle);
        hostActivity.replaceFragment(galleryFragment);



    }
}
