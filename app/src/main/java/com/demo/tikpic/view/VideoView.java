package com.demo.tikpic.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.demo.tikpic.viewpager.ViewPagerFragment;

import java.io.IOException;

public class VideoView extends RelativeLayout implements TextureView.SurfaceTextureListener {

    //	private TextureView textureView;
    private MediaPlayer mMediaPlayer;
    private Surface surface;
    private String path;
    TextureView textureView;
    private int screenWidth, screenHight, mScreenOrientation;
    private Context mContext;

    public VideoView(Context context, String path) {
        super(context);
        mContext=context;
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

        ViewPagerFragment.mo=1;

        textureView = new TextureView(getContext());
        textureView.setSurfaceTextureListener(this);
        textureView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.start();
            }
        });
        addView(textureView);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(getContext(), Uri.parse(path));
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //mMediaPlayer.start();
            }
        });
        mMediaPlayer.prepare();
        getWindewSize();
        setSize();
    }
     private void getWindewSize() {
        WindowManager mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHight = metrics.heightPixels;
        mScreenOrientation = mWindowManager.getDefaultDisplay().getRotation();
     /*   if (mScreenOrientation == 1 || mScreenOrientation == 3) {
            screenWidth = metrics.heightPixels;
            screenHight = metrics.widthPixels;
        }*/
    }

    private void setSize(){
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();


        float ww = 1.0f;
        if (screenHight < videoHeight || screenWidth < videoWidth) {
            ww = (float) screenWidth / (float) videoWidth;
            float hh = (float) screenHight / (float) videoHeight;
            if (ww > hh) {
                ww = hh;
            }
        } else if (screenHight > videoHeight && screenWidth > videoWidth) {
            ww = (float) screenWidth / (float) videoWidth;
            float hh = (float) screenHight / (float) videoHeight;
            if (ww > hh) {
                ww = hh;
            }
        }
        int w = (int) (videoWidth * ww);
        int h = (int) (videoHeight * ww);
        LayoutParams layoutParams = new LayoutParams(w, h);
        //layoutParams.setMargins((screenWidth-w)/2,(screenHight-h)/2,screenWidth-(screenWidth-w)/2,screenHight-(screenHight-h)/2);
        layoutParams.setMargins((screenWidth - w) / 2, (screenHight - h) / 2, 0, 0);
        textureView.setLayoutParams(layoutParams);
    }
    private void play() {

        mMediaPlayer.setSurface(surface);

        mMediaPlayer.start();
        mMediaPlayer.pause();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceT, int width, int height) {
        surface = new Surface(surfaceT);
        play();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceT) {
        surfaceT = null;
        surface = null;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
