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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        MainActivity hostActivity = (MainActivity) getActivity();

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        DataAdapter dataAdapter = new DataAdapter(hostActivity);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(dataAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(hostActivity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        return root;
    }
}