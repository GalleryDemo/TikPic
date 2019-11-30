package com.demo.tikpic.ViewPager;

import android.annotation.SuppressLint;
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

import java.io.FileInputStream;
import java.io.IOException;

public class ImageDisplayView extends View {

    private int mState;//显示状态 合适屏幕，原图，或自由状态

    private Matrix mMatrix = new Matrix();//绘制前最终变换矩阵
    //上次缩放倍率
    private float mMatrixScale;
    private float[] mMatrixTranslate = new float[2];

    private String TAG = "ImageDisplayView";

    private int[] mScreenSize = new int[2];

    private ImageLoader mLoader;
    private ImageResoutce mImage;
    private DisplayWindow mDisplayWindow;


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
                    imageMove(mInputDetector.dx * num_moveSpeed, mInputDetector.dy * num_moveSpeed);
                    break;
                case 2:
                    double newDist = mInputDetector.distance;
                    imageZoom(((float) newDist + num_zoomSensitivity) / ((float) mInputDetector.lastDist + num_zoomSensitivity), mInputDetector.middle);
                    imageMove(mInputDetector.dx * num_moveSpeed, mInputDetector.dy * num_moveSpeed);
                    mInputDetector.lastDist = newDist;
                    break;
                case 6:

                case 7:

                default:
                    break;
            }
            return true;
        }
    };

    public ImageDisplayView(Context context) {
        super(context);
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
        getWindowInfo();

        mLoader = new ImageLoader();
        mMatrixScale = 1.0f;

        mState = 1;
        mInputDetector = new InputDetector(mListener);

    }

    private class ImageResoutce {
        BitmapRegionDecoder resource;
        //width:0  height:1
        int[] imageSize = new int[2];

        //basicBlockSize是采样率1时分块大小
        //采样率在缩放屏幕过程中会改变，接着改变当前采样率时的块大小，数量，边界
        int sampleSize;
        int[] basicBlockSize = new int[2];
        int[] adaptBlockSize = new int[2];
        int[] numBlocks = new int[2];
        int[] edgeBlockLength = new int[2];

        ImageResoutce(Uri uri) {
            setImageUri(uri);
            sampleSize = 1;
            setBasicBlockSize(1024);
            setSampleSize(1);
        }

        void setBasicBlockSize(int n) {
            basicBlockSize[0] = n;
            basicBlockSize[1] = n;
            setSampleSize(1);
        }

        void setSampleSize(int n) {
            sampleSize = n;
            for (int i = 0; i < 2; i++) {
                adaptBlockSize[i] = basicBlockSize[i] * n;
                numBlocks[i] = imageSize[i] / adaptBlockSize[i];
                edgeBlockLength[i] = imageSize[i] - numBlocks[i] * adaptBlockSize[i];

            }
        }


        private void setImageUri(Uri uri) {
            try {
                FileInputStream f = (FileInputStream) getContext().getContentResolver().openInputStream(uri);
                resource = BitmapRegionDecoder.newInstance(f, false);
                imageSize[0] = resource.getWidth();
                imageSize[1] = resource.getHeight();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DisplayWindow {
        Bitmap bitmap;
        int[] posion = new int[2];
        int[] size = new int[2];
        int[] lastSize = new int[]{-1, -1};
    }

    private boolean updatewindowSize(float scale) {
        for (int i = 0; i < 2; i++) {
            mDisplayWindow.lastSize[i] = mDisplayWindow.size[i];
            mDisplayWindow.size[i] = 1 + (int) Math.ceil((mScreenSize[i] / scale) / mImage.adaptBlockSize[i]);
            if (mDisplayWindow.size[i] > mImage.numBlocks[i]) {
                mDisplayWindow.size[i] = mImage.numBlocks[i] + 1;
            }
        }

        if (mDisplayWindow.lastSize[0] == mDisplayWindow.size[0] && mDisplayWindow.lastSize[1] == mDisplayWindow.size[1]) {
            return false;
        } else {
            Log.d(TAG, "setSize: displaysize   " + mDisplayWindow.size[0] + "   /   " + mDisplayWindow.size[1]);
            Log.d(TAG, "updatewindowSize: adaptBlockSize" + mImage.adaptBlockSize[0]);
            mDisplayWindow.bitmap = Bitmap.createBitmap(mImage.adaptBlockSize[0] * mDisplayWindow.size[0], mImage.adaptBlockSize[1] * mDisplayWindow.size[1], Bitmap.Config.RGB_565);
            return true;
        }

    }

    public void setUri(Uri uri) {
        mImage = new ImageResoutce(uri);
        mImage.setBasicBlockSize(1024);
        mDisplayWindow = new DisplayWindow();
        mState = 1;
        displayEffect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        int specMode = View.MeasureSpec.getMode(widthMeasureSpec);
        //获取测量大小
        int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
//        int specMode2 = View.MeasureSpec.getMode(heightMeasureSpec);
        //获取测量大小
        int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "onMeasure: " + specSizeHeight + "/" + specSizeWidth);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mScreenSize[0] != specSizeWidth || mScreenSize[1] != specSizeHeight) {
            mScreenSize[0] = specSizeWidth;
            mScreenSize[1] = specSizeHeight;
            displayEffect();
        }

    }

    private void block() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        Canvas middleCanvas = new Canvas(mDisplayWindow.bitmap);
        middleCanvas.drawColor(0xFFFFFFFF);
        Log.d(TAG, "block: mDisplayWindow.posion[0] " + mDisplayWindow.posion[0] + "," + mDisplayWindow.posion[1]);
        Log.d(TAG, "block: block.size[0]  " + (mImage.numBlocks[0] + 1) + "," + (mImage.numBlocks[1] + 1));
        Log.d(TAG, "block: mDisplayWindow.size[0] " + mDisplayWindow.size[0] + "," + mDisplayWindow.size[1]);
        for (int i = 0; i < 2; i++) {
            if (mDisplayWindow.posion[i] + mDisplayWindow.size[i] > mImage.numBlocks[i] + 1) {
                mDisplayWindow.posion[i] = mImage.numBlocks[i] + 1 - mDisplayWindow.size[i];
            }
        }
        for (int i = mDisplayWindow.posion[0], ni = 0; ni < mDisplayWindow.size[0]; i++, ni++) {
            for (int j = mDisplayWindow.posion[1], nj = 0; nj < mDisplayWindow.size[1]; j++, nj++) {

                final String key = mImage.sampleSize + "/" + i + "/" + j;
                //Log.d(TAG, "block: " + key);
                Bitmap blockBitmap = mLoader.getBitmap(key);

                //边缘块的长宽需要计算，不管有没有
                final int thisWidth = (i == mImage.numBlocks[0]) ? mImage.edgeBlockLength[0] : mImage.adaptBlockSize[0];
                final int thisHeight = (j == mImage.numBlocks[1]) ? mImage.edgeBlockLength[1] : mImage.adaptBlockSize[1];
                if (blockBitmap == null) {
                    // Log.d(TAG, "block: addbitmap"+key);
                    //正常块的长宽直接乘
                    final int ii = i;
                    final int jj = j;

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                int rectLeft = ii * block.basicBlockSize;
//                                int rectTop = jj * block.basicBlockSize;
//                                Rect blockRect = new Rect(rectLeft, rectTop, rectLeft + thisWidth, rectTop + thisHeight);
//                                long begin =System.currentTimeMillis();
//                                Bitmap blockBitmap = originalImage.decodeRegion(blockRect, options);
//                                long end =System.currentTimeMillis();
//                                mLoader.addBitmap(key, blockBitmap);
//                                invalidate();
//                            }
//                        }).start();

                    int rectLeft = ii * mImage.adaptBlockSize[0];
                    int rectTop = jj * mImage.adaptBlockSize[1];
                    Rect blockRect = new Rect(rectLeft, rectTop, rectLeft + thisWidth, rectTop + thisHeight);
                    blockBitmap = mImage.resource.decodeRegion(blockRect, options);
                    mLoader.addBitmap(key, blockBitmap);
                }
                int rectLeft = ni * mImage.adaptBlockSize[0];
                int rectTop = nj * mImage.adaptBlockSize[1];

                Rect blockRect = new Rect(rectLeft, rectTop, rectLeft + thisWidth, rectTop + thisHeight);
                if (blockBitmap != null) {
                    middleCanvas.drawBitmap(blockBitmap, null, blockRect, null);
                }
            }


        }


    }

    private void getWindowInfo() {
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (mWindowManager != null) {
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
        }
        mScreenSize[0] = metrics.widthPixels;
        mScreenSize[1] = metrics.heightPixels;
        Log.d(TAG, "getWindowInfo: " + mScreenSize[0] + " / " + mScreenSize[1]);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawARGB(255,0,0,0);
        canvas.drawColor(0xFFFFFFFF);
        updateLocation();
        canvas.drawBitmap(mDisplayWindow.bitmap, mMatrix, null);
    }


    private void displayEffect() {
        switch (mState) {
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


    private float caculateRatio() {
        float ww = (float) mImage.imageSize[0] / (float) mScreenSize[0];
        float hh = (float) mImage.imageSize[1] / (float) mScreenSize[1];
        if (ww < 1 && hh < 1) {
            //return 1.0f;
            return ww > hh ? ww : hh;
        } else {
            return ww > hh ? ww : hh;
        }
    }

    private void displaySuitable() {
        float ratio = caculateRatio();
        mMatrix = new Matrix();
        imageZoom(1 / ratio, new Point(0, 0));
        mState = 1;
    }


    private void displayOriginal() {
        imageZoom(1 / mMatrixScale, new Point((int) mInputDetector.x, (int) mInputDetector.y));
        mState = 2;
    }

    private void setZoom(float x) {
        mMatrix.preScale(x, x);
    }

    private void setTranslate(float x, float y) {
        mMatrix.postTranslate((int) x, (int) y);
    }


    private void imageZoom(float x, Point point) {
        mMatrixScale = mMatrixScale * x;
        boolean update1 = false;
        Log.d(TAG, "imageZoom: " + mMatrixScale);
        //检查倍率
        if (mMatrixScale <= 1 / (float) (2 * mImage.sampleSize) && mImage.numBlocks[0] > 1 && mImage.numBlocks[1] > 1) {
            mImage.setSampleSize(mImage.sampleSize * 2);
            mDisplayWindow.posion = new int[]{0, 0};
            update1 = true;
        } else if (mMatrixScale >= 1 / (float) (2 * (mImage.sampleSize - 1))) {
            if (mImage.sampleSize != 1) {
                mImage.setSampleSize(mImage.sampleSize / 2);
            }
            update1 = true;
        }
        boolean update2 = updatewindowSize(mMatrixScale);
        if (update1 || update2) {
            block();
        }
        setZoom(x);

        float nleft = ((point.x - mMatrixTranslate[0] + mDisplayWindow.posion[0] * mImage.adaptBlockSize[0]) / (mImage.imageSize[0] * mMatrixScale)) * mImage.imageSize[0] * mMatrixScale * x - point.x;
        float ntop = ((point.y - mMatrixTranslate[1] + mDisplayWindow.posion[1] * mImage.adaptBlockSize[1]) / (mImage.imageSize[1] * mMatrixScale)) * mImage.imageSize[1] * mMatrixScale * x - point.y;

        float dx = -nleft - mMatrixTranslate[0];
        float dy = -ntop - mMatrixTranslate[1];
        float[] dxy = caculateBoundary(dx, dy);
        setTranslate(dxy[0], dxy[1]);
        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // invalidate();

        return mInputDetector.onTouchEvent(event);

    }

    private void onClick() {
        switch (mState) {
            case 1:
                mState = 2;
                break;
            case 2:
                mState = 1;
                break;
            default:
                mState = 1;
                break;
        }
        displayEffect();
    }

    private void imageMove(float dx, float dy) {
        float[] dxy = caculateBoundary(dx, dy);
        //float[] dxy = new float[]{dx,dy};

//        long begin = System.currentTimeMillis();
        float[] transXY = new float[2];
        //x轴偏移超过一格
        boolean isBlockChanged = false;
        for (int i = 0; i < 2; i++) {
            if ((-mMatrixTranslate[i] + dxy[i] + mScreenSize[i]) / mMatrixScale > mDisplayWindow.size[i] * mImage.adaptBlockSize[i]) {
                if (mDisplayWindow.posion[i] <= mImage.numBlocks[i] - mDisplayWindow.size[i]) {
                    mDisplayWindow.posion[i]++;
                    transXY[i] = mImage.adaptBlockSize[i] * mMatrixScale;
                    isBlockChanged = true;
                }
            } else if (mMatrixTranslate[i] + dxy[i] > 0.0f) {
                if (mDisplayWindow.posion[i] > 0) {
                    mDisplayWindow.posion[i]--;
                    transXY[i] = -mImage.adaptBlockSize[i] * mMatrixScale;
                    isBlockChanged = true;
                }
            }
        }
        if (isBlockChanged) {
            setTranslate(transXY[0] + dxy[0], transXY[1] + dxy[1]);
            // Log.d(TAG, "imageMove: change");


            new Thread(new Runnable() {
                @Override
                public void run() {
                    block();
                    invalidate();
                }
            }).start();
            // block();


        } else {
            Log.d(TAG, "imageMove: dxdy" + dxy[0] + "  /  " + dxy[1]);
            setTranslate(dxy[0], dxy[1]);
        }

        invalidate();
//        long end = System.currentTimeMillis();
        // Log.d(TAG, "imageMove: time"+(end-begin));
    }

    //更新缩放倍率和偏移记录
    private void updateLocation() {
        float[] values = new float[9];
        mMatrix.getValues(values);
        mMatrixScale = values[0];
        mMatrixTranslate[0] = values[2];
        mMatrixTranslate[1] = values[5];
    }

    private float[] caculateBoundary(float dx, float dy) {
        updateLocation();
        //居中放置，如果有一瞬间超过屏幕产生边界就消除边界
        float[] dxy = {dx, dy};
        for (int i = 0; i < 2; i++) {
            if (mScreenSize[i] >= mImage.imageSize[i] * mMatrixScale) {
                Log.d(TAG, "caculateBoundary: " + mMatrixTranslate[i]);
                dxy[i] = (mScreenSize[i] - (mImage.imageSize[i] * mMatrixScale)) / 2 - mMatrixTranslate[i];
            } else if (mMatrixTranslate[i] + dxy[i] > 0 && mDisplayWindow.posion[i] == 0) {

                dxy[i] = -mMatrixTranslate[i];
            } else if (-mMatrixTranslate[i] + mScreenSize[i] + mDisplayWindow.posion[i] * mImage.adaptBlockSize[i] * mMatrixScale > mImage.imageSize[i] * mMatrixScale ||
                    -mMatrixTranslate[i] + mScreenSize[i] - dxy[i] + mDisplayWindow.posion[i] * mImage.adaptBlockSize[i] * mMatrixScale > mImage.imageSize[i] * mMatrixScale) {
                dxy[i] = mScreenSize[i] - (mImage.imageSize[i] * mMatrixScale - mDisplayWindow.posion[i] * mImage.adaptBlockSize[i] * mMatrixScale) - mMatrixTranslate[i];
            }
        }
        return dxy;
    }

}
