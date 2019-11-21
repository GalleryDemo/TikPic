package com.demo.tikpic;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;

public class ImageFragment extends Fragment {

    private Context context;
    private ImageView imageView;
    private String uri;

    static Fragment newInstance(String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uri = getArguments().getString("uri");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        imageView = view.findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentActivity)context).supportFinishAfterTransition();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Glide.with(context)
                .asBitmap()
                .load(uri)
                // .apply(new RequestOptions().dontAnimate())
                .into(imageView);
    }

    View getSharedElement() {
        return imageView;
    }
}
