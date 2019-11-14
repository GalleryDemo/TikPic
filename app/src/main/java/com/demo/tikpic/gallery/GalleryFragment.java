package com.demo.tikpic.gallery;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;

public class GalleryFragment extends Fragment {

    private MainActivity hostActivity;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        hostActivity = (MainActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new DataAdapter(hostActivity));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(hostActivity, 4);
        recyclerView.setLayoutManager(gridLayoutManager);

        return root;
    }
}