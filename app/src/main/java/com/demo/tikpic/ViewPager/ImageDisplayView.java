package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.demo.tikpic.MainActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ImageDisplayView extends View {
    private MainActivity mActivity;

    private BitmapRegionDecoder originalImage ;
    private Rect rect;
    private BitmapFactory.Options options;

    private Bitmap mBitmap;  //缓存绘制的内容
    private Matrix matrix = new Matrix();//绘制前最终变换矩阵

    private List<Integer> mlist;
    private int index;

    private int state;//显示状态 合适屏幕，原图，或自由状态

    //上次缩放倍率
    private float translatex, translatey, zoomRatio;

    private String TAG = "ImageDisplayView";

    private int screenHeight, screenWidth, height, width;

    //手势
    private InputDetector inputDetector;
    private InputDetector.OnInputListener listener = new InputDetector.OnInputListener() {
        @Override
        public boolean action(int n) {
            //Log.d(TAG, "action: " + n);
            int num_zoomSensitivity = 0;
            float num_moveSpeed = 3.0f;
            updateLocation();
            switch (n) {
                case 4:
                    onClick();
                    break;
                case 3:
                    imageMove(inputDetector.dx * num_moveSpeed, inputDetector.dy * num_moveSpeed);

                    break;
                case 2:
                    double newDist = inputDetector.distance;
                    imageZoom(((float) newDist + num_zoomSensitivity) / ((float) inputDetector.lastDist + num_zoomSensitivity), inputDetector.middle);
                    imageMove(inputDetector.dx * num_moveSpeed, inputDetector.dy * num_moveSpeed);
                    inputDetector.lastDist = newDist;
                    break;
                case 6:
                    if (translatey >= 0) {
                        nextImage(2);
                    } else {
                        imageMove(inputDetector.dx * num_moveSpeed, inputDetector.dy * num_moveSpeed);
                    }
                    break;
                case 7:
                    if (-translatey + screenHeight >= mBitmap.getHeight() * zoomRatio) { //下边界
                        nextImage(1);
                    } else {
                        imageMove(inputDetector.dx * num_moveSpeed, inputDetector.dy * num_moveSpeed);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };


    public ImageDisplayView(Context context, MainActivity mActivity) {
        super(context);
        this.mActivity = mActivity;
        init();
    }

    public ImageDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageDisplayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (mWindowManager != null) {
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
        }
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        state = 1;

        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 2;

        inputDetector = new InputDetector(listener);

        mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, matrix, null);

    }


    private void setImagePath(Uri uri) {
        try {
            InputStream f = getContext().getContentResolver().openInputStream(uri);
            originalImage = BitmapRegionDecoder.newInstance(f, false);
            height = originalImage.getHeight();
            width = originalImage.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAlbumImage(int pic, int album) {

        //设置要显示的相册列表及图片序号
        //mlist = mActivity.data.getShowcaseOrAlbumOrIndex(0,album);
        index = pic;
        //需要在主列表里做转换
        Uri uri = Uri.parse(mActivity.data.getShowcaseOrAlbumOrIndex(0,album, pic).getPath());

        Log.d(TAG, "setAlbumImage: "+uri.getPath());
        setImagePath(uri);

        rect = new Rect(0, 0, width, height);
        options.inSampleSize = 1;
        load();

        state = 1;
        displayEffect();

    }


    //test
    public void setImage(String pic) {
        //设置要显示的相册列表及图片序号
        //需要在主列表里做转换
        String imagePath = pic;

        //setImagePath(imagePath);

        rect = new Rect(0, 0, width, height);
        options.inSampleSize = 1;
        load();

        state = 1;
        displayEffect();

    }

    private void displayEffect() {
        switch (state) {
            case 1://合适尺度
                displaySuitable();
                break;
            case 2:
                displayOriginal();
                break;
            default:
                break;
        }
    }


    private void load() {
        mBitmap = originalImage.decodeRegion(rect, options);
    }

    private float caculateRatio() {
        float ww = (float) width / (float) screenWidth;
        float hh = (float) height / (float) screenHeight;
        if (ww < 1 && hh < 1) {
            //return 1.0f;
            return ww > hh ? ww : hh;
        } else {
            return ww > hh ? ww : hh;
        }
    }

    private void displaySuitable() {
        float ratio = caculateRatio();
        matrix = new Matrix();
        imageZoom(1 / ratio, new Point((int) inputDetector.x, (int) inputDetector.y));
        state = 1;

    }

    private void nextImage(int order) {

    }


    private void displayOriginal() {
        imageZoom(1 / zoomRatio, new Point((int) inputDetector.x, (int) inputDetector.y));
        state = 2;
    }

    private void setZoom(float x) {
        matrix.preScale(x, x);
    }

    private void setTranslate(float x, float y) {
        matrix.postTranslate(x, y);
    }


    private void dynamicLoading() {
        //动态加载：扫描当前看的倍率及窗口，加载适合内存大小的文件图


         /*
        if(options.inSampleSize!=1&&zoomRatio>1){
            rect = new Rect(0, 0, width, height);
            //根据屏幕尺寸计算载入倍率
            options.inSampleSize=lastSize-1;
            Log.d(TAG, "imageZoom: option"+options.inSampleSize);
            load();

            setZoom(zoomRatio/lastSize*(lastSize-1));
            invalidate();
            displayBriefMemory();
            return;
        }*/
        //Log.d(TAG, "imageZoom: now" + matrix.toString());
    }

    private void imageZoom(float x, Point point) {
        state = 3;
        updateLocation();

        setZoom(x);

        float nleft = ((point.x - translatex) / (mBitmap.getWidth() * zoomRatio)) * mBitmap.getWidth() * zoomRatio * x - point.x;
        float ntop = ((point.y - translatey) / (mBitmap.getHeight() * zoomRatio)) * mBitmap.getHeight() * zoomRatio * x - point.y;


        float dy = -ntop - translatey;
        float dx = -nleft - translatex;

        float[] dxy = caculateBoundary(dx, dy);

        setTranslate(dxy[0], dxy[1]);
        invalidate();

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return inputDetector.onTouchEvent(event);
    }

    private void onClick() {
        switch (state) {
            case 1:
                state = 2;
                break;
            case 2:
                state = 1;
                break;
            default:
                state = 1;
                break;
        }
        displayEffect();
    }

    private void imageMove(float dx, float dy) {

        float[] dxy = caculateBoundary(dx, dy);

        setTranslate(dxy[0], dxy[1]);


        if (Math.abs(dxy[1]) > 0) {
            inputDetector.in67 = false;
        }
        invalidate();
    }

    //更新缩放倍率和偏移记录
    private void updateLocation() {
        float[] values = new float[9];
        matrix.getValues(values);
        zoomRatio = values[0];
        translatex = values[2];
        translatey = values[5];
    }

    private float[] caculateBoundary(float dx, float dy) {
        updateLocation();
        //居中放置，如果有一瞬间超过屏幕产生边界就消除边界

        if (screenHeight >= mBitmap.getHeight() * zoomRatio) {
            dy = (screenHeight - (mBitmap.getHeight() * zoomRatio)) / 2 - translatey;
        } else if (translatey + dy > 0) {
            dy = -translatey;
        } else if (-translatey + screenHeight > mBitmap.getHeight() * zoomRatio || -translatey - dy + screenHeight > mBitmap.getHeight() * zoomRatio) {
            dy = (screenHeight - mBitmap.getHeight() * zoomRatio - translatey);
        }

        if (screenWidth >= mBitmap.getWidth() * zoomRatio) {
            dx = (screenWidth - (mBitmap.getWidth() * zoomRatio)) / 2 - translatex;
        } else if (translatex + dx > 0) {
            dx = -translatex;
        } else if (-translatex + screenWidth > mBitmap.getWidth() * zoomRatio || -translatex - dx + screenWidth > mBitmap.getWidth() * zoomRatio) {
            dx = (screenWidth - mBitmap.getWidth() * zoomRatio - translatex);
        }
        return new float[]{dx, dy};
    }

}
