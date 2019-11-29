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

import java.io.FileInputStream;
import java.io.IOException;

public class ImageDisplayView extends View {


    //private Bitmap window_mBitmap;  //缓存绘制的内容


    private int state;//显示状态 合适屏幕，原图，或自由状态


    private Matrix matrix = new Matrix();//绘制前最终变换矩阵
    //上次缩放倍率
    private float matrixScale;
    private float[] matrixTranslate = new float[2];

    private String TAG = "ImageDisplayView";

    private int[] screenSize = new int[2];

    private ImageLoader loader;
    private ImageResoutce mImage;
    private DisplayWindow mDisplayWindow;

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

        public ImageResoutce(Uri uri) {
            setImageUri(uri);
            sampleSize = 1;
            setBasicBlockSize(2048);
            setSampleSize(1);
        }

        public void setBasicBlockSize(int n) {
            basicBlockSize[0] = n;
            basicBlockSize[1] = n;
            setSampleSize(1);
        }

        public void setSampleSize(int n) {
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
        private Bitmap bitmap;
        int[] posion = new int[2];
        int[] size = new int[2];
        int[] lastSize = new int[]{-1, -1};

        public DisplayWindow() {
        }


    }


    private boolean updateindowSize( float scale) {
        for (int i = 0; i < 2; i++) {
            mDisplayWindow.lastSize[i] = mDisplayWindow.size[i];
            mDisplayWindow.size[i] = 1+(int)Math.ceil((screenSize[i] / scale) / mImage.adaptBlockSize[i]);
            if(mDisplayWindow.size[i]>mImage.numBlocks[i]){
                mDisplayWindow.size[i] =mImage.numBlocks[i]+1;
            }
        }

        if(mDisplayWindow.lastSize[0] == mDisplayWindow.size[0] && mDisplayWindow.lastSize[1] == mDisplayWindow.size[1]){
            return false;
        }else{
            Log.d(TAG, "setSize: displaysize   " + mDisplayWindow.size[0] + "   /   " + mDisplayWindow.size[1]);
            Log.d(TAG, "updateindowSize: adaptBlockSize"+mImage.adaptBlockSize[0] );
            mDisplayWindow.bitmap = Bitmap.createBitmap(mImage.adaptBlockSize[0] * mDisplayWindow.size[0], mImage.adaptBlockSize[1] * mDisplayWindow.size[1], Bitmap.Config.RGB_565);
            return true;
        }

    }

    //手势
    private InputDetector inputDetector;
    private InputDetector.OnInputListener listener = new InputDetector.OnInputListener() {
        @Override
        public boolean action(int n) {
            //Log.d(TAG, "action: " + n);
            int num_zoomSensitivity = 0;
            float num_moveSpeed = 1.0f;
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
                    break;
                case 7:
                    break;
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

        loader = new ImageLoader();
        matrixScale = 1.0f;

        state = 1;
        inputDetector = new InputDetector(listener);

    }


    public void setUri(Uri uri) {
        mImage = new ImageResoutce(uri);

        mImage.setBasicBlockSize(1024);
        mDisplayWindow = new DisplayWindow();
        updateindowSize(matrixScale);

        block();
        invalidate();

    }


    private void block() {


//        updateLocation();
//        Log.d(TAG, "block: " + translatex + "/" + translatey + "/" + matrixScale);
//
//
//

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;


        Canvas middleCanvas = new Canvas(mDisplayWindow.bitmap);
        Log.d(TAG, "block: mDisplayWindow.posion[0] "+mDisplayWindow.posion[0] +","+mDisplayWindow.posion[1]);
        Log.d(TAG, "block: block.size[0] "+mImage.numBlocks[0] +","+mImage.numBlocks[1]);
        Log.d(TAG, "block: mDisplayWindow.size[0] "+mDisplayWindow.size[0] +","+mDisplayWindow.size[1]);
        for(int i =0;i<2;i++){
            if(mDisplayWindow.posion[i]+mDisplayWindow.size[i]>mImage.numBlocks[i]+1){
                mDisplayWindow.posion[i]=mImage.numBlocks[i]+1-mDisplayWindow.size[i];
            }
        }


        for (int i = mDisplayWindow.posion[0], ni = 0; ni < mDisplayWindow.size[0]; i++, ni++) {
            for (int j = mDisplayWindow.posion[1], nj = 0; nj < mDisplayWindow.size[1]; j++, nj++) {

                final String key = mImage.sampleSize + "/" + i + "/" + j;
                //Log.d(TAG, "block: " + key);
                Bitmap blockBitmap = loader.getBitmap(key);

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
//                                loader.addBitmap(key, blockBitmap);
//                                invalidate();
//                            }
//                        }).start();

                    int rectLeft = ii * mImage.adaptBlockSize[0];
                    int rectTop = jj * mImage.adaptBlockSize[1];
                    Rect blockRect = new Rect(rectLeft, rectTop, rectLeft + thisWidth, rectTop + thisHeight);

                    blockBitmap = mImage.resource.decodeRegion(blockRect, options);

                    loader.addBitmap(key, blockBitmap);

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
        screenSize[0] = metrics.widthPixels;
        screenSize[1] = metrics.heightPixels;
        Log.d(TAG, "getWindowInfo: " + screenSize[0] + " / " + screenSize[1]);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawARGB(255,0,0,0);
        canvas.drawBitmap(mDisplayWindow.bitmap, matrix, null);
    }


    private void displayEffect() {
        switch (state) {
            case 1://合适尺度
                //           displaySuitable();
                break;
            case 2:
                displayOriginal();
                break;
            default:
                break;
        }
    }


//    private float caculateRatio() {
//        float ww = (float) width / (float) screenWidth;
//        float hh = (float) height / (float) screenHeight;
//        if (ww < 1 && hh < 1) {
//            //return 1.0f;
//            return ww > hh ? ww : hh;
//        } else {
//            return ww > hh ? ww : hh;
//        }
//    }

//    private void displaySuitable() {
//        float ratio = caculateRatio();
//        matrix = new Matrix();
//        imageZoom(1 / ratio, new Point((int) inputDetector.x, (int) inputDetector.y));
//        state = 1;
//        //mActivity.mo=0;
//
//    }


    private void displayOriginal() {
        imageZoom(1 / matrixScale, new Point((int) inputDetector.x, (int) inputDetector.y));
        state = 2;
    }

    private void setZoom(float x, Point point) {
        matrix.preScale(x, x, point.x, point.y);
    }

    private void setTranslate(float x, float y) {
        matrix.postTranslate((int) x, (int) y);
    }


    private void imageZoom(float x, Point point) {
        matrixScale = matrixScale * x;
        boolean update1 = false;
        Log.d(TAG, "imageZoom: " + matrixScale );
        //检查倍率
        if (matrixScale <= 0.5f&&mImage.numBlocks[0]>1&&mImage.numBlocks[1]>1) {
            mImage.setSampleSize(mImage.sampleSize * 2);
            mDisplayWindow.posion=new int[]{0,0};
            update1 = true;
        } else if (matrixScale >= 2.0f) {
            if (mImage.sampleSize != 1) {
                mImage.setSampleSize(mImage.sampleSize / 2);
            }
            update1 = true;
        }
        boolean update2=updateindowSize(matrixScale);
        if (update1||update2) {
            block();
        }
        setZoom(x, point);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // invalidate();
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
        //float[] dxy = caculateBoundary(dx, dy);
        float[] dxy = {dx, dy};
        long begin = System.currentTimeMillis();

        float[] transXY = new float[2];
        //x轴偏移超过一格
        boolean isBlockChanged = false;
        for (int i = 0; i < 2; i++) {
            if ((-matrixTranslate[i] + dxy[i] + screenSize[i]) / matrixScale > mDisplayWindow.size[i] * mImage.adaptBlockSize[i]) {
                if (mDisplayWindow.posion[i] <= mImage.numBlocks[i] - mDisplayWindow.size[i]) {
                    mDisplayWindow.posion[i]++;
                    transXY[i] = mImage.adaptBlockSize[i] * matrixScale;
                    isBlockChanged = true;
                }
            } else if (matrixTranslate[i] + dxy[i] > 0.0f) {
                if (mDisplayWindow.posion[i] > 0) {
                    mDisplayWindow.posion[i]--;
                    transXY[i] = -mImage.adaptBlockSize[i] * matrixScale;
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
            setTranslate(dxy[0], dxy[1]);
        }

        invalidate();
        long end = System.currentTimeMillis();
        // Log.d(TAG, "imageMove: time"+(end-begin));
    }

    //更新缩放倍率和偏移记录
    private void updateLocation() {
        float[] values = new float[9];
        matrix.getValues(values);
        matrixScale = values[0];
        matrixTranslate[0] = values[2];
        matrixTranslate[1] = values[5];
    }

//    private float[] caculateBoundary(float dx, float dy) {
//        updateLocation();
//        //居中放置，如果有一瞬间超过屏幕产生边界就消除边界
//
//        if (screenHeight >= height * matrixScale) {
//            dy = (screenHeight - (height * matrixScale)) / 2 - translatey;
//        } else if (translatey + dy > 0) {
//            dy = -translatey;
//        } else if (-translatey + screenHeight > height * matrixScale || -translatey - dy + screenHeight > height * matrixScale) {
//            dy = (screenHeight - height * matrixScale - translatey);
//        }
//
//        if (screenWidth >= width * matrixScale) {
//            dx = (screenWidth - (width * matrixScale)) / 2 - translatex;
//        } else if (translatex + dx > 0) {
//            dx = -translatex;
//        } else if (-translatex + screenWidth > width * matrixScale || -translatex - dx + screenWidth > width * matrixScale) {
//            dx = (screenWidth - width * matrixScale - translatex);
//        }
//        return new float[]{dx, dy};
//    }

}
