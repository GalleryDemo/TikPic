package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.IOException;

public class VideoView extends RelativeLayout implements TextureView.SurfaceTextureListener {
    //	private TextureView textureView;
    private MediaPlayer mMediaPlayer;
    private Surface surface;
    private String path;

    public VideoView(Context context) {
        super(context);
    }

    public VideoView(Context context, String path) {
        super(context);
        this.path = path;
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void init() throws IOException {
        TextureView textureView = new TextureView(getContext());
        textureView.setSurfaceTextureListener(this);
        textureView.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                mMediaPlayer.start();
            }
        });
        addView(textureView);

        mMediaPlayer= new MediaPlayer();
        mMediaPlayer.setDataSource(getContext(), Uri.parse(path));
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp){
                //mMediaPlayer.start();
            }
        });
        mMediaPlayer.prepare();

    }

    private void play(){

        mMediaPlayer.setSurface(surface);

        mMediaPlayer.start();
        mMediaPlayer.pause();
    }



    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceT, int width, int height) {
        surface=new Surface(surfaceT);
        play();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceT) {
        surfaceT=null;
        surface=null;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
