package com.demo.tikpic.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demo.tikpic.R;
import com.demo.tikpic.viewpager.GestureViewPager;
import com.demo.tikpic.viewpager.ViewPagerFragment;

import java.io.IOException;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class VideoView extends RelativeLayout implements TextureView.SurfaceTextureListener {

    String TAG = "VideoView";
    //	private TextureView textureView;
    private MediaPlayer mMediaPlayer;
    private Surface surface;
    private String path;
    TextureView textureView;
    private int screenWidth, screenHight, mScreenOrientation;
    private Context mContext;
    ImageView mPlayIcon;
    ImageButton button;
    SeekBar seekBar;
    private TextView text_now, text_all;
    private Thread thread;
    private int CurrentPosition;
    private boolean flag_play;
    float scale = 1.0f;
    int videoWidth, videoHeight;

    private Thread thr() {
        return new Thread() {
            @Override
            public void run() {
                //super.run();
                while (flag_play) {
                    if (CurrentPosition != mMediaPlayer.getCurrentPosition()) {

                        CurrentPosition = mMediaPlayer.getCurrentPosition();
                        seekBar.setProgress(CurrentPosition);

                    }
                }

            }
        };
    }

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

                seekBar.setMax(mMediaPlayer.getDuration());

                text_all.setText(getShowTime(mMediaPlayer.getDuration()));
                flag_play = true;

                thread = thr();
                thread.start();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayIcon.setVisibility(View.VISIBLE);
                seekBar.setProgress(mMediaPlayer.getDuration());
            }
        });

        mMediaPlayer.prepare();
        getWindewSize();

        videoWidth = mMediaPlayer.getVideoWidth();
        videoHeight = mMediaPlayer.getVideoHeight();

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

        setSize(ww);
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


    @SuppressLint("ResourceType")
    private void anotherView() {
        mPlayIcon = new ImageView(mContext);

        mPlayIcon.setImageResource(R.drawable.video_icon_normal);//设置图片


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                dip2px(mContext, 150), dip2px(mContext, 150));
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        mPlayIcon.setLayoutParams(lp);//设置布局参数
        addView(mPlayIcon);//RelativeLayout添加子View

        VideoToolbar videoToolbar = new VideoToolbar(mContext);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        videoToolbar.setLayoutParams(lp2);//设置布局参数
        addView(videoToolbar);

        button = findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();

                    mPlayIcon.setVisibility(View.VISIBLE);
                } else {
                    mMediaPlayer.start();

                    mPlayIcon.setVisibility(View.INVISIBLE);
                }
            }
        });


        text_now = (TextView) findViewById(R.id.text_now);
        text_all = (TextView) findViewById(R.id.text_all);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if (progress >= 0 && mMediaPlayer.isPlaying()) {
                    // 如果是用户手动拖动控件，则设置视频跳转。
                    if (fromUser) {
                        mMediaPlayer.seekTo(progress);
                    }
                    // 设置当前播放时间

                } else {
                    if (fromUser) {
                        mMediaPlayer.seekTo(progress);
                    }
                }
                text_now.setText(getShowTime(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
        });
    }

    private String getShowTime(Integer n) {
        String s = "";
        //n = n / 1000;
        int nn;
        nn = n / 1000 / 60;
        if (nn < 10) {
            s += "0";
        }
        s += nn + ":";
        nn = n / 1000;
        if (nn < 10) {
            s += "0";
        }
        s += nn;
        return s;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void setSize(float x) {
        scale *= x;
        int w = (int) (videoWidth * scale);
        int h = (int) (videoHeight * scale);
        Log.d(TAG, "setSize: " + w + "  /  " + h);
        LayoutParams layoutParams = new LayoutParams(w, h);
        //layoutParams.setMargins((screenWidth-w)/2,(screenHight-h)/2,screenWidth-(screenWidth-w)/2,screenHight-(screenHight-h)/2);
        layoutParams.setMargins((screenWidth - w) / 2, (screenHight - h) / 2, 0, 0);
        textureView.setLayoutParams(layoutParams);
    }

    private void play() {

        mMediaPlayer.setSurface(surface);
        mMediaPlayer.seekTo(0);
        mPlayIcon.setVisibility(VISIBLE);
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
        flag_play = false;
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

            switch (n) {
                case 4:
                    onClick();
                    break;
                case 3:
                    //move
                    GestureViewPager.setGestureSwitchTrue();
                    break;
                case 2:
                    //zoom
                    double newDist = mInputDetector.distance;
                    setSize(((float) newDist) / ((float) mInputDetector.lastDist));
                    mInputDetector.lastDist = newDist;
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
            mPlayIcon.setVisibility(View.VISIBLE);
        } else {
            mMediaPlayer.start();
            mPlayIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mInputDetector.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return false;//return onTouchEvent(ev);
    }

    public void reset() {
        if (mMediaPlayer.isPlaying()) {

            // mMediaPlayer.pause();
            mMediaPlayer.seekTo(0);
            mMediaPlayer.pause();
            mPlayIcon.setVisibility(View.VISIBLE);

        }
    }

}
