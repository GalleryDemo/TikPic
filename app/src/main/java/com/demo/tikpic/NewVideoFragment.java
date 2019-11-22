package com.demo.tikpic;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class NewVideoFragment extends Fragment {

    private static final String TAG = "MYTEST";

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;
    private Button playButton;
    private SeekBar seekBar;

    private TextView currentPositionText;
    private TextView duration;

    private Surface mSurface;
    private String uri;

    private boolean isFirstTime;
    private boolean isPlaying;
    private int currentPosition;

    static Fragment newInstance(String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        NewVideoFragment fragment = new NewVideoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        uri = getArguments().getString("uri");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video_new, container, false);
        mTextureView = rootView.findViewById(R.id.textureView);

        mMediaPlayer = new MediaPlayer();
        currentPosition = 0;
        isFirstTime = true;

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurface = new Surface(surface);
                new Thread(new MediaPlaybackRunnable()).start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        playButton = rootView.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFirstTime) {
                    isFirstTime = false;
                    isPlaying = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (isPlaying) {
                                if (currentPosition != mMediaPlayer.getCurrentPosition()) {
                                    currentPosition = mMediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(currentPosition);
                                }
                            }
                            Log.d(TAG, "run: seekbar sync thread stopped");
                        }
                    }).start();
                }

                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                else {
                    mMediaPlayer.start();
                }
            }
        });

        seekBar = rootView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mMediaPlayer.seekTo(progress);
                }
                currentPositionText.setText(formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch: started");

                mMediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: stopped");
                int process = seekBar.getProgress();
                if(mMediaPlayer != null) {
                    mMediaPlayer.seekTo(process);
                }
                mMediaPlayer.start();
            }
        });

        duration = rootView.findViewById(R.id.duration);
        currentPositionText = rootView.findViewById(R.id.current_pos);

        return rootView;
    }

    private class MediaPlaybackRunnable implements Runnable {
        @Override
        public void run() {
            try {
                mMediaPlayer.setSurface(mSurface);
                mMediaPlayer.setDataSource(mContext, Uri.parse(uri));
                mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        // adjust the size of textureView to fit the video

                        float textureWidth = (float) mTextureView.getWidth();
                        float textureHeight = (float) mTextureView.getHeight();

                        float sx = textureWidth / (float) width;
                        float sy = textureHeight / (float) height;

                        Matrix matrix = new Matrix();

                        matrix.preTranslate((textureWidth - width) / 2,
                                            (textureHeight - height) / 2 );

                        matrix.preScale(width / textureWidth, height / textureHeight);

                        if (sx >= sy) {
                            matrix.postScale(sy, sy, textureWidth / 2, textureHeight / 2);
                        }
                        else {
                            matrix.postScale(sx, sx, textureWidth / 2, textureHeight / 2);
                        }

                        mTextureView.setTransform(matrix);
                        mTextureView.postInvalidate();
                    }
                });
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // mMediaPlayer.start();
                        mMediaPlayer.seekTo(1);
                        mMediaPlayer.setLooping(true);
                        seekBar.setMax(mMediaPlayer.getDuration());

                        duration.setText(formatTime(mMediaPlayer.getDuration()));
                    }
                });
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String formatTime(int millis) {
        int durationInSecond = millis / 1000;
        int second = durationInSecond % 60;
        int minute = durationInSecond / 60;
        return String.format("%02d:", minute) + String.format("%02d", second);
    }


    public void pauseVideo() {
        mMediaPlayer.pause();
    }

    public void stopSeekBarSyncThread() {
        isPlaying = false;
        Log.d(TAG, "stopSeekBarSyncThread: isPlaying switched to false");
    }

}
