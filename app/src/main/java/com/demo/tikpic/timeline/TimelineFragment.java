package com.demo.tikpic.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.tikpic.MainActivity;
import com.demo.tikpic.R;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class TimelineFragment extends Fragment implements PhotoSection.ClickListener {

    private MainActivity hostActivity;
    private SectionedRecyclerViewAdapter sectionedAdapter;

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

        sectionedAdapter = new SectionedRecyclerViewAdapter();

        final LoadPhotos loadPhotos = new LoadPhotos(hostActivity);
        for(String albumName : loadPhotos.getAlbumKeySet()) {
            sectionedAdapter.addSection(
                    new PhotoSection(albumName,
                            loadPhotos.getPhotoListInAlbum(albumName),
                            hostActivity, this));
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
        Toast.makeText(
                getContext(),
                String.format(
                        "Clicked on position #%s of Section %s",
                        sectionedAdapter.getPositionInSection(itemAdapterPosition),
                        sectionTitle), Toast.LENGTH_SHORT).show();
    }

}