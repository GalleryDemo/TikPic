package com.demo.tikpic.gallery;


import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private MainActivity hostActivity;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        hostActivity = (MainActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new DataAdapter(hostActivity, getImageUrlList()));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        return rootView;
    }

    private List<String> getImageUrlList() {
        List<String> imageList = new ArrayList<>();
        Cursor cursor = hostActivity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        .buildUpon().appendPath(String.valueOf(id)).build().toString();
                imageList.add(uri);
            }
            cursor.close();
        }

        return imageList;
    }

}