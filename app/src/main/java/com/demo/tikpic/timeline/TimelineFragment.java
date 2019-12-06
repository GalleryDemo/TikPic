package com.demo.tikpic.timeline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;
import com.demo.tikpic.ViewPagerActivity;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class TimelineFragment extends Fragment implements PhotoSection.ClickListener {


    private static final String TAG = "TimelineFragment";
    private MainActivity hostActivity;
    private MyAdapter sectionedAdapter;
    private int mScreenWidth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        hostActivity = (MainActivity) getActivity();

        final View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mScreenWidth = metrics.widthPixels;

        int itemWidth = mScreenWidth/4;

        sectionedAdapter = new MyAdapter();

        final LoadPhotos loadPhotos = new LoadPhotos(hostActivity);
        final DataManager dataManager = DataManager.getInstance(hostActivity);

        for(String albumName : dataManager.getDateAlbumNames()) {
            Log.d(TAG, "onCreateView: " + albumName);
            sectionedAdapter.addSection(
                    new PhotoSection(albumName,
                            dataManager.getPhotoListInAlbum(albumName),
                            hostActivity, this,itemWidth));
        }

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        final GridLayoutManager glm = new GridLayoutManager(hostActivity, 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                if (sectionedAdapter.getSectionItemViewType(position)
                        == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                    return 3;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(sectionedAdapter);

        return view;
    }

    @Override
    public void onHeaderRootViewClicked(@NonNull final String sectionTitle,
                                        @NonNull final PhotoSection section) {
        Toast.makeText(
                getContext(),
                String.format("Clicked on more button from the header of Section %s", sectionTitle),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onItemRootViewClicked(@NonNull final String sectionTitle,
                                      final int itemAdapterPosition) {
        /*Toast.makeText(
                getContext(),
                String.format(
                        "Clicked on position #%s of Section %s",
                        sectionedAdapter.getPositionInSection(itemAdapterPosition),
                        sectionTitle), Toast.LENGTH_SHORT).show();*/

        Intent intent = new Intent(hostActivity, ViewPagerActivity.class);
        intent.putExtra("position", itemAdapterPosition);
        Log.d("DEBUGALL", "onClick - photoAdapter - itemAdapterPosition:  "+itemAdapterPosition);
        hostActivity.startActivity(intent);
    }

}

