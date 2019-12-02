package com.demo.tikpic.viewpager;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import java.io.IOException;

public class VideoDisplayView extends RelativeLayout {

    Uri uri;
    String TAG = "VideoDisplayView";
    private Context mContext;
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;

    private int screenWidth, screenHight, mScreenOrientation;
    private int CurrentPosition;


    public VideoDisplayView(Context context) {
        super(context);
        init(context);
    }

    public VideoDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoDisplayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        mContext = context;
        surfaceView = new SurfaceView(mContext);
        addView(surfaceView);

        //anotherWidget();

        getWindewSize();

    }

    private void anotherWidget() {
        Button button = new Button(mContext);
        button.setText("pause");
        LayoutParams a = new LayoutParams(1000, 1000);
        button.setLayoutParams(a);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        addView(button);
    }

    public void setResourse(String path) {
        uri = Uri.parse(path);
        start();
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

    private void start() {

        player = new MediaPlayer();
        try {
            player.setDataSource(mContext, uri);

            holder = surfaceView.getHolder();
            holder.addCallback(new MyCallBack());

            playerBegin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void playerBegin() {
        //   player.prepare();
        player.prepareAsync();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                player.start();

            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                player.pause();

                //player.release();
                //player = null;


            }
        });
    }

    private class MyCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            int videoWidth = player.getVideoWidth();
            int videoHeight = player.getVideoHeight();


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
            surfaceView.setLayoutParams(layoutParams);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
