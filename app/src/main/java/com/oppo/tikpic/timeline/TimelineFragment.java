package com.oppo.tikpic.timeline;

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

import com.oppo.tikpic.R;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class TimelineFragment extends Fragment implements PhotoSection.ClickListener {

    private SectionedRecyclerViewAdapter sectionedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        sectionedAdapter = new SectionedRecyclerViewAdapter();

        final LoadPhotos loadPhotos = new LoadPhotos(getActivity());
        for(String album : loadPhotos.getAlbumList()) {
            sectionedAdapter.addSection(
                    new PhotoSection(album, loadPhotos.execute(album), getContext(), this));
        }

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        final GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                if (sectionedAdapter.getSectionItemViewType(position)
                        == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(sectionedAdapter);


        return view;
    }

    @Override
    public void onHeaderRootViewClicked(@NonNull final String sectionTitle, @NonNull final PhotoSection section) {
        Toast.makeText(
                getContext(),
                String.format(
                        "Clicked on more button from the header of Section %s",
                        sectionTitle
                ),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onItemRootViewClicked(@NonNull final String sectionTitle, final int itemAdapterPosition) {
        Toast.makeText(
                getContext(),
                String.format(
                        "Clicked on position #%s of Section %s",
                        sectionedAdapter.getPositionInSection(itemAdapterPosition),
                        sectionTitle
                ),
                Toast.LENGTH_SHORT
        ).show();
    }

}