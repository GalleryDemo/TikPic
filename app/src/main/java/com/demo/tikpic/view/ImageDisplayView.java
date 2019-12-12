package com.demo.tikpic.view;

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

import com.demo.tikpic.viewpager.GestureViewPager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDisplayView extends View {
    private String TAG = "ImageDisplayView";
    private String mId;

    private int mState;//显示状态 合适屏幕，原图

    private Matrix mMatrix = new Matrix();//绘制前最终变换矩阵
    private float mMatrixScale;
    private float mScale;
    private float[] mMatrixTranslate = new float[2];

    private int[] mScreenSize = new int[2];

    private ImageLoader mLoader;
    private ImageResource mImage;
    private DisplayWindow mDisplayWindow;

    private int mColor = 0xFF000000;
    private boolean mFlagCanDraw = false;
    private int mStateMoveZoom = 0;

    private Map<String, String> mBlockState = new HashMap<>();

    //缩放限制开关
    private int mScaleLimit = 1;
    private ExecutorService mCachedThreadPool = Executors.newCachedThreadPool();

    private Map<String, Bitmap> mBaseBitmap = new HashMap<>();


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
                    break;
                case 8:
                    onClick();
                    break;
                case 3:
                    imageMove(mInputDetector.dx * num_moveSpeed, mInputDetector.dy * num_moveSpeed);
                    mStateMoveZoom = 1;
                    break;
                case 2:
                    double newDist = mInputDetector.distance;
                    imageZoom(((float) newDist + num_zoomSensitivity) / ((float) mInputDetector.lastDist
                            + num_zoomSensitivity), mInputDetector.middle);
                    //imageMove(mInputDetector.dx * num_moveSpeed, mInputDetector.dy * num_moveSpeed);
                    mInputDetector.lastDist = newDist;
                    mStateMoveZoom = 1;
                    break;
                case 6:

                case 7:

                default:
                    break;
            }
            return true;
        }
    };

    private class ImageResource {
        BitmapRegionDecoder resource;
        //width:0  height:1
        int[] imageSize = new int[2];
        boolean isSmallImage = false;

        //basicBlockSize是采样率1时分块大小
        //采样率在缩放屏幕过程中会改变，接着改变当前采样率时的块大小，数量，边界
        int sampleSize;
        int[] basicBlockSize = new int[2];
        int[] adaptBlockSize = new int[2];
        int[] numBlocks = new int[2];
        int[] edgeBlockLength = new int[2];

        ImageResource(Uri uri) {
            setImageUri(uri);
            sampleSize = 1;
            setBasicBlockSize(1024);
        }

        void setBasicBlockSize(int n) {
            basicBlockSize[0] = n;
            basicBlockSize[1] = n;
            setSampleSize(1);
        }

        void setBasicBlockSize(int w, int h) {
            basicBlockSize[0] = w;
            basicBlockSize[1] = h;
            setSampleSize(1);
        }


        void setSampleSize(int n) {
            sampleSize = n;
            for (int i = 0; i < 2; i++) {
                adaptBlockSize[i] = basicBlockSize[i] * n;
                numBlocks[i] = (int) Math.ceil((float) imageSize[i] / adaptBlockSize[i]) - 1;
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

        mLoader = ImageLoader.getInstance();
        mScale = 1.0f;

        mState = 1;
        mInputDetector = new InputDetector(mListener);

    }

    private boolean updatewindowSize(float scale) {
        for (int i = 0; i < 2; i++) {
            mDisplayWindow.lastSize[i] = mDisplayWindow.size[i];
            mDisplayWindow.size[i] = 1 + (int) Math.ceil((mScreenSize[i] / scale) / mImage.adaptBlockSize[i]);
            if (mDisplayWindow.size[i] > mImage.numBlocks[i]) {
                mDisplayWindow.size[i] = mImage.numBlocks[i] + 1;
            }
        }

        if (mDisplayWindow.lastSize[0] == mDisplayWindow.size[0] &&
                mDisplayWindow.lastSize[1] == mDisplayWindow.size[1]) {
            return false;
        } else {
            // Log.d(TAG, "setSize: displaysize   " + mDisplayWindow.size[0] + "   /   " + mDisplayWindow.size[1]);
            //Log.d(TAG, "updatewindowSize: adaptBlockSize" + mImage.adaptBlockSize[0]);
            mDisplayWindow.bitmap = Bitmap.createBitmap(mImage.basicBlockSize[0] * mDisplayWindow.size[0],
                    mImage.basicBlockSize[1] * mDisplayWindow.size[1], Bitmap.Config.RGB_565);

            mBlockState.clear();

            return true;
        }

    }
    InputStream f = null;
    public void setUri(Uri uri) {


     //   long begin = System.currentTimeMillis();
        try {
             f = (FileInputStream) getContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
       // Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
     //   long end = System.currentTimeMillis();

      //  Log.d(TAG, "openInputStream(uri);  ----------------: " + "  time :" + (end - begin));

        mId = uri.toString();
        mImage = new ImageResource(uri);
        //Log.d(TAG, "setUri: " + uri.getPath());
        mDisplayWindow = new DisplayWindow();
        mState = 1;

        //Log.d(TAG, "setUri: " + mImage.imageSize[0] + "    ///   " + mImage.imageSize[1]);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doFirst();
                //displayEffect();
            }
        }).start();
    }

    private void block() {
        if (mDisplayWindow.bitmap == null) {
            return;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        // options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = mImage.sampleSize;

        Canvas middleCanvas = new Canvas(mDisplayWindow.bitmap);

        //直接填充颜色会覆盖原图
        //middleCanvas.drawColor(mColor);

//        Log.d(TAG, "block: mDisplayWindow.posion[0] " + mDisplayWindow.posion[0] + "," + mDisplayWindow.posion[1]);
//        Log.d(TAG, "block: block.size[0]  " + (mImage.numBlocks[0] + 1) + "," + (mImage.numBlocks[1] + 1));
//        Log.d(TAG, "block: mDisplayWindow.size[0] " + mDisplayWindow.size[0] + "," + mDisplayWindow.size[1]);
        for (int i = 0; i < 2; i++) {
            if (mDisplayWindow.posion[i] + mDisplayWindow.size[i] > mImage.numBlocks[i] + 1) {
                mDisplayWindow.posion[i] = mImage.numBlocks[i] + 1 - mDisplayWindow.size[i];
            }
        }
        for (int i = mDisplayWindow.posion[0], ni = 0; ni < mDisplayWindow.size[0]; i++, ni++) {
            for (int j = mDisplayWindow.posion[1], nj = 0; nj < mDisplayWindow.size[1]; j++, nj++) {

                final String key = mId + "_" + mImage.sampleSize + "/" + i + "/" + j;

                if (mBlockState.containsKey(mId + "_" + mImage.sampleSize + "//" + ni + "/" + nj)) {
                    if (mBlockState.get(mId + "_" + mImage.sampleSize + "//" + ni + "/" + nj).compareTo(key) == 0) {
                        continue;
                    }
                }
                //Log.d(TAG, "block: key " + key);

                Bitmap blockBitmap = mLoader.getBitmap(key);
                if(blockBitmap==null){
                    blockBitmap = mBaseBitmap.get(key);
                }
                String key2 = "";
                if (blockBitmap == null) {
                    //Log.d(TAG, "block: " + key + "  块不存在");
                    for (int k = mImage.sampleSize * 2, l = 2; k <= caculateSampleSize(caculateRatio()); k *= 2, l *= 2) {
                        key2 = mId + "_" + k + "/" + (i / l) + "/" + (j / l);

                        blockBitmap = mLoader.getBitmap(key2);
                        if (k == caculateSampleSize(caculateRatio()) && blockBitmap == null) {
                            blockBitmap = mBaseBitmap.get(key2);

                        }
                        if (blockBitmap != null) {
                            int x = i % l;
                            int y = j % l;
                            int blocksize = mImage.adaptBlockSize[0] / l;
                            Matrix xx = new Matrix();
                            xx.setScale(k / mImage.sampleSize, k / mImage.sampleSize);

                            //这块超出边界
                            if ((blockBitmap.getWidth() <= blocksize * x) || (blockBitmap.getHeight() <= blocksize * y)) {
                                blockBitmap = null;
                                break;
                            }
                            int w = blocksize;
                            int h = blocksize;
                            if ((x + 1) * blocksize > blockBitmap.getWidth()) {
                                w = blockBitmap.getWidth() - x * blocksize;
                            }
                            if ((y + 1) * blocksize > blockBitmap.getHeight()) {
                                h = blockBitmap.getHeight() - y * blocksize;
                            }

                            blockBitmap = Bitmap.createBitmap(blockBitmap, x * blocksize, y * blocksize, w, h, xx, false);
                            break;
                        }
                    }


                    if (!mBlockState.containsKey(key)) {
                        // 1 表示没有加载过
                        mBlockState.put(key, "1");
                        //正常块的长宽直接乘
                        final int ii = i;
                        final int jj = j;
                        mCachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                int thisWidth = (ii == mImage.numBlocks[0]) ? mImage.edgeBlockLength[0] :
                                        mImage.adaptBlockSize[0];
                                int thisHeight = (jj == mImage.numBlocks[1]) ? mImage.edgeBlockLength[1] :
                                        mImage.adaptBlockSize[1];
                                int rectLeft = ii * mImage.adaptBlockSize[0];
                                int rectTop = jj * mImage.adaptBlockSize[1];
                                if (rectLeft + thisWidth > mImage.imageSize[0] || rectTop + thisHeight >
                                        mImage.imageSize[1]) {
                                    return;
                                } else {
                                    Rect blockRect = new Rect(rectLeft, rectTop, rectLeft + thisWidth,
                                            rectTop + thisHeight);
                                    long begin = System.currentTimeMillis();
                                    Bitmap blockBitmap = mImage.resource.decodeRegion(blockRect, options);
                                    long end = System.currentTimeMillis();

                                    Log.d(TAG, "block: "+key + "  time :" + (end - begin));

                                    mLoader.addBitmap(key, blockBitmap);
                                    block();
                                }
                            }
                        });
                    }

                } else {
                    //2表示在缓存线程中
                    mBlockState.remove(key);
                }

                if (blockBitmap != null) {
                    int rectLeft = (ni * mImage.adaptBlockSize[0]) / mImage.sampleSize;
                    int rectTop = nj * mImage.adaptBlockSize[1] / mImage.sampleSize;
                    if (key2 == "") {
                        mBlockState.put(mId + "_" + mImage.sampleSize + "//" + ni + "/" + nj, key);
                    } else {

                    }
                    middleCanvas.drawBitmap(blockBitmap, rectLeft, rectTop, null);
                    mFlagCanDraw = true;
                    invalidate();
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
        //Log.d(TAG, "getWindowInfo: " + mScreenSize[0] + " / " + mScreenSize[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mColor);
        updateLocation();
        if (mFlagCanDraw) {
            canvas.drawBitmap(mDisplayWindow.bitmap, mMatrix, null);
        }

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

    private void displaySuitable() {
        float ratio = caculateRatio();
        mScale = 1.0f;
        mMatrix = new Matrix();
        imageZoom(ratio, new Point(0, 0));
        mState = 1;
    }

    private Map<String, Bitmap> mBasicBitmap = new HashMap<>();

    private void doFirst() {

        //小图
        Log.d(TAG, "doFirst: szie  "+mImage.imageSize[0]*mImage.imageSize[1]);
         if(mImage.imageSize[0]*mImage.imageSize[1]<12000000){
            mImage.setBasicBlockSize(mImage.imageSize[0], mImage.imageSize[1]);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize =1;
            long begin = System.currentTimeMillis();
            mDisplayWindow.bitmap = mImage.resource.decodeRegion(new Rect(0, 0, mImage.imageSize[0], mImage.imageSize[1]), options);
            long end = System.currentTimeMillis();
            Log.d(TAG, "耗时11111  ----------------: " + "  time :" + (end - begin));
            Log.d(TAG, "doFirst: wh7 "+mImage.imageSize[0]+"  //  "+mImage.imageSize[1]);

        } else {
             float ratio = caculateRatio();
             // Log.d(TAG, "loadBasicBitmap: "+ratio);
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inJustDecodeBounds = false;
             options.inPreferredConfig = Bitmap.Config.RGB_565;

             options.inSampleSize=caculateSampleSize(ratio);

             Rect blockRect = new Rect(0, 0, mImage.imageSize[0], mImage.imageSize[1]);

             long begin = System.currentTimeMillis();
             Bitmap xxxx = mImage.resource.decodeRegion(blockRect, options);
             long end = System.currentTimeMillis();
             Log.d(TAG, "run: memory   "+(float)xxxx.getAllocationByteCount()/1024/1024);
             Log.d(TAG, "loadBasicBitmap: " + "  time :" + (end - begin));


             mImage.setSampleSize(options.inSampleSize);
             for (int i = 0; i <= mImage.numBlocks[0]; i++) {
                 for (int j = 0; j <= mImage.numBlocks[1]; j++) {
                     int thisWidth = (i == mImage.numBlocks[0]) ? mImage.edgeBlockLength[0] / options.inSampleSize :
                             mImage.basicBlockSize[0];
                     int thisHeight = (j == mImage.numBlocks[1]) ? mImage.edgeBlockLength[1] / options.inSampleSize :
                             mImage.basicBlockSize[1];
                     int rectLeft = i * mImage.basicBlockSize[0];
                     int rectTop = j * mImage.basicBlockSize[1];
                     Bitmap add = Bitmap.createBitmap(xxxx, rectLeft, rectTop, thisWidth, thisHeight, null, false);
                     String key2 = mId + "_" + options.inSampleSize + "/" + i + "/" + j;
                     Log.d(TAG, "doFirst: keyyyyyyy"+key2);
                     mBaseBitmap.put(key2, add);
                 }
             }

        }
         displayEffect();
    }

    private void displayOriginal() {
        imageZoom(1 / mScale, new Point((int) mInputDetector.x, (int) mInputDetector.y));
        mState = 2;
    }

    private void setZoom(float x) {
        mMatrix.preScale(x, x);
    }

    private void setTranslate(float x, float y) {
        mMatrix.postTranslate((int) x, (int) y);
    }

    private int caculateSampleSize(float scale) {
        boolean flag = true;
        int sampleSize = 1;
        while (flag) {
            if (scale <= 1 / (float) (2 * sampleSize) && mImage.basicBlockSize[0] * sampleSize <=
                    mImage.imageSize[0] && mImage.basicBlockSize[1] * sampleSize < mImage.imageSize[1]) {
                sampleSize *= 2;

            } else if (scale >= 1 / (float) (2 * (sampleSize / 2))) {
                if (sampleSize != 1) {
                    sampleSize /= 2;
                } else {
                    flag = false;
                }
            } else {
                flag = false;
            }
        }
        return sampleSize;
    }


    private void imageZoom(float x, Point point) {

        float newMatrixScale = mScale * x;

        if (mScaleLimit == 1) {
            if (caculateRatio() <= 1.0f) {
                if (newMatrixScale > 1.0f) {
                    newMatrixScale = 1.0f;
                    x = 1 / mScale;
                } else if (newMatrixScale < caculateRatio()) {
                    newMatrixScale = caculateRatio();
                    x = caculateRatio() / mScale;
                }
            } else {
                if (newMatrixScale < 1.0f) {
                    newMatrixScale = 1.0f;
                    x = 1 / mScale;
                } else if (newMatrixScale > caculateRatio()) {
                    newMatrixScale = caculateRatio();
                    x = caculateRatio() / mScale;
                }
            }

        }

        boolean update1 = false;
        //检查倍率
        int sampleSize = mImage.sampleSize;
        //如果切换到了下一个倍率
        int newSampleSize = caculateSampleSize(newMatrixScale);
        Log.d(TAG, "imageZoom: newSampleSizenewSampleSize"+newSampleSize+"   "+newMatrixScale);
        if (newSampleSize != sampleSize) {
            mImage.setSampleSize(newSampleSize);
            update1 = true;
        }
        boolean update2 = updatewindowSize(newMatrixScale);
        if (update1 || update2) {
            block();
        }
        updateLocation();
        setZoom((newMatrixScale * newSampleSize) / mMatrixScale);

        mScale = newMatrixScale;

        float nleft = ((point.x - mMatrixTranslate[0] + mDisplayWindow.posion[0] * mImage.adaptBlockSize[0]) /
                (mImage.imageSize[0] * mScale)) * mImage.imageSize[0] * mScale * x - point.x;
        float ntop = ((point.y - mMatrixTranslate[1] + mDisplayWindow.posion[1] * mImage.adaptBlockSize[1]) /
                (mImage.imageSize[1] * mScale)) * mImage.imageSize[1] * mScale * x - point.y;

        float dx = -nleft - mMatrixTranslate[0];
        float dy = -ntop - mMatrixTranslate[1];

        imageMove(dx, dy);

    }

    private void imageMove(float dx, float dy) {
        float[] dxy = caculateBoundary(dx, dy);
        //Log.d(TAG, "imageMove: caculate dxy  "+dxy[0]+"  //  "+dxy[1]);
        float[] transXY = new float[]{0.0f, 0.0f};
        //x轴偏移超过一格
        boolean isBlockChanged = false;
        for (int i = 0; i < 2; i++) {
            while (((-mMatrixTranslate[i] - dxy[i] - transXY[i] + mScreenSize[i]) / mScale >
                    mDisplayWindow.size[i] * mImage.adaptBlockSize[i]) &&
                    (mDisplayWindow.posion[i] <= mImage.numBlocks[i] - mDisplayWindow.size[i])) {
                mDisplayWindow.posion[i]++;
                transXY[i] += mImage.adaptBlockSize[i] * mScale;
                isBlockChanged = true;
            }
            while ((mMatrixTranslate[i] + dxy[i] + transXY[i] > 0.0f) && (mDisplayWindow.posion[i] > 0)) {
                mDisplayWindow.posion[i]--;
                transXY[i] += -mImage.adaptBlockSize[i] * mScale;
                isBlockChanged = true;
            }
        }
        if (isBlockChanged) {
            setTranslate(transXY[0] + dxy[0], transXY[1] + dxy[1]);
            block();

        } else {
            setTranslate(dxy[0], dxy[1]);
        }

        invalidate();
    }

    //更新缩放倍率和偏移记录
    private void updateLocation() {
        float[] values = new float[9];
        mMatrix.getValues(values);
        mMatrixScale = values[0];
        mMatrixTranslate[0] = values[2];
        mMatrixTranslate[1] = values[5];
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // invalidate();
        // final int action = event.getAction() & MotionEvent.ACTION_MASK;
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

    private float[] caculateBoundary(float dx, float dy) {
        updateLocation();
        //居中放置，如果有一瞬间超过屏幕产生边界就消除边界
        float[] dxy = {dx, dy};
        //flag=0表示开启viewPager切换
        int flag = 1;
        for (int i = 0; i < 2; i++) {
            //屏幕过大时居中
            //移动到左上边缘时阻止
            //移动到右下边缘时阻止
            if (mScreenSize[i] >= mImage.imageSize[i] * mScale) {
                dxy[i] = (mScreenSize[i] - (mImage.imageSize[i] * mScale)) / 2 - mMatrixTranslate[i];
                if (i == 0 && Math.abs(dx) > 0.01f) {
                    flag = 0;
                }
            } else if (mMatrixTranslate[i] + dxy[i] > 0 && mDisplayWindow.posion[i] == 0) {
                dxy[i] = -mMatrixTranslate[i];
                if (i == 0 && Math.abs(dx) > 0.01f) {
                    flag = 0;
                }
            } else if (-mMatrixTranslate[i] + mScreenSize[i] + mDisplayWindow.posion[i] * mImage.adaptBlockSize[i] * mScale > mImage.imageSize[i] * mScale ||
                    -mMatrixTranslate[i] + mScreenSize[i] - dxy[i] + mDisplayWindow.posion[i] * mImage.adaptBlockSize[i] * mScale > mImage.imageSize[i] * mScale) {
                dxy[i] = mScreenSize[i] - (mImage.imageSize[i] * mScale - mDisplayWindow.posion[i] * mImage.adaptBlockSize[i] * mScale) - mMatrixTranslate[i];
                if (dxy[i] > -1.0f && dxy[i] < 0.0f) {
                    dxy[i] = -1.0f;
                } else if (dxy[i] < 1.0f && dxy[i] > 0.0f) {
                    dxy[i] = 1.0f;
                }
                if (i == 0 && Math.abs(dx) > 0.01f) {
                    flag = 0;
                }
            }
        }

        if (mStateMoveZoom == 1 && flag == 0) {
            GestureViewPager.setGestureSwitchTrue();
        }

        return dxy;
    }

    private float caculateRatio() {
        float ww = (float) mScreenSize[0] / (float) mImage.imageSize[0];
        float hh = (float) mScreenSize[1] / (float) mImage.imageSize[1];
        return ww < hh ? ww : hh;
    }

    public void reset() {
        displaySuitable();
    }

    public void destroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCachedThreadPool.shutdown();
                mDisplayWindow.bitmap.recycle();
                mDisplayWindow.bitmap = null;
                mImage.resource.recycle();
                mImage.resource = null;
            }
        }).start();

    }

}
