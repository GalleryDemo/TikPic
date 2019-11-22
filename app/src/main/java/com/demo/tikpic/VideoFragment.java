package com.demo.tikpic;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VideoFragment extends Fragment {

    private static final String TAG = "VideoFragment";
    private Context context;
    private VideoView mVideoView;
    private MediaController controller;
    private String uri;
    private String tag;


    static Fragment newInstance(String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        VideoFragment fragment = new VideoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        uri = getArguments().getString("uri");
        tag = uri.substring(uri.lastIndexOf('/') + 1);
        Log.d(TAG, "onAttach: entered " + tag);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: entered " + tag);
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: entered " + tag);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        mVideoView = view.findViewById(R.id.videoView);
        controller = new MediaController(context);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: entered " + tag);
        super.onStart();
        initializePlayer();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: entered " + tag);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: entered " + tag);
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: entered " + tag);
        super.onStop();
        releasePlayer();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: entered " + tag);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: entered " + tag);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: entered " + tag);
        super.onDetach();
    }

    private void initializePlayer() {

        Uri videoUri = getMedia(uri);
        mVideoView.setVideoURI(videoUri);

        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        // mediaPlayer.setScreenOnWhilePlaying();
                        // mediaPlayer.setWakeMode();
                        mVideoView.seekTo(1);
                        // mVideoView.start();
                    }
                });
        controller.hide();
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    private Uri getMedia(String mediaName) {
        if (URLUtil.isValidUrl(mediaName)) {
            // media name is an external URL
            return Uri.parse(mediaName);
        } else { // media name is a raw resource embedded in the app
            return Uri.parse("android.resource://" + context.getPackageName() +
                    "/raw/" + mediaName);
        }
    }

    public void pauseVideo() {
        mVideoView.pause();
    }
}
