package com.demo.tikpic.viewpager;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class GestureViewPager extends ViewPager {
    private static final String TAG = "NewViewPager";

    private int flag = 0;
    private int flagfx;
    private float x, y, lastx, lasty;

    //true对手势起反应
    static private boolean mGestureSwitch;



    public GestureViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public GestureViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        this.setOnPageChangeListener(new OnPageChangeListener() {
            int pos = 0;
            int lastPos = -1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //Log.d(TAG, "onPageSelected: " + position+"  / " +lastPos);
                pos = position;
                GestureViewPagerAdapter adapter = (GestureViewPagerAdapter) getAdapter();
                if (lastPos != -1) {
                    adapter.resume(lastPos);
                } else {
                    adapter.resume(0);
                }
                lastPos = pos;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mGestureSwitch && action == MotionEvent.ACTION_MOVE) {
            //需要移动的第一个瞬间
            if (flag == 0) {
                flag = 1;
                long time = SystemClock.uptimeMillis();//必须是 SystemClock.uptimeMillis()。
                MotionEvent downEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                super.onInterceptTouchEvent(downEvent);
                super.onTouchEvent(downEvent);
                x = ev.getX();
                y = ev.getY();
                // Log.d(TAG, "onInterceptTouchEvent: "+(lastx-x));
                if (lastx - x > 0) {
                    flagfx = 1;
                } else {
                    flagfx = 2;
                }
            }
        }
        lastx = ev.getX();
        lasty = ev.getY();
        if ((flagfx == 1 && lastx > x) || (flagfx == 2 && lastx < x)) {
            long time = SystemClock.uptimeMillis();
            MotionEvent downEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, ev.getX(), ev.getY(), 0);

            onTouchEvent(downEvent);
        }

        if (flag == 1) {
            //super.onInterceptTouchEvent(ev);
            onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP) {
            super.onTouchEvent(ev);
            super.onInterceptTouchEvent(ev);
            mGestureSwitch=false;

            flag = 0;
            flagfx = 0;
        }
        return super.onTouchEvent(ev);
    }

    static public void setGestureSwitchTrue(){
        mGestureSwitch=true;
    }
}
