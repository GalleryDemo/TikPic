package com.demo.tikpic.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.demo.tikpic.R;
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
    ImageView item1;

    public VideoView(Context context, String path) {
        super(context);
        mContext = context;
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

        mInputDetector = new InputDetector(mListener);


        textureView = new TextureView(getContext());
        textureView.setSurfaceTextureListener(this);

        addView(textureView);

        anotherView();

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

    private void anotherView() {
        item1 = new ImageView(mContext);

        item1.setImageResource(R.drawable.video_icon_normal);//设置图片


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
               dip2px(mContext,150), dip2px(mContext,150));
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        item1.setLayoutParams(lp);//设置布局参数
        addView(item1);//RelativeLayout添加子View
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    private void setSize() {
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
        item1.setVisibility(VISIBLE);
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


    //手势
    private InputDetector mInputDetector;
    private InputDetector.OnInputListener mListener = new InputDetector.OnInputListener() {
        @Override
        public boolean action(int n) {
            //Log.d(TAG, "action: " + n);
            int num_zoomSensitivity = 0;
            float num_moveSpeed = 1.0f;

            switch (n) {
                case 4:
                    onClick();
                    break;
                case 3:
                    //move
                    ViewPagerFragment.mo = 0;
                    break;
                case 2:
                    //zoom
                    break;

                default:
                    break;
            }
            return true;
        }
    };

    private void onClick() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();

            item1.setVisibility(View.VISIBLE);
        } else {
            mMediaPlayer.start();

            item1.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mInputDetector.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouchEvent(ev);
    }

    public void reset() {
        if (mMediaPlayer.isPlaying()) {

            // mMediaPlayer.pause();
            mMediaPlayer.pause();
            item1.setVisibility(View.VISIBLE);
            mMediaPlayer.seekTo(0);

        }
    }
}
