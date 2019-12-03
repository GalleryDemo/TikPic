package com.demo.tikpic.viewpager;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.demo.tikpic.MainActivity;

public class NewViewPager extends ViewPager {
    private static final String TAG = "NewViewPager";

    int flag = 0;
    static int end = 0;

    public NewViewPager(@NonNull Context context) {
        super(context);
        init();
    }


    public NewViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setOnPageChangeListener(new OnPageChangeListener() {
            int pos = 0;
            int beginScroll = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d(TAG, "onPageScrolled: "+position+"/"+positionOffset+"/"+positionOffsetPixels);
//                if(positionOffset<0.1f){
//                    beginScroll=1;
//                }else if(positionOffset>0.9f){
//                    beginScroll=2;
//                }
//                if( beginScroll==1&&positionOffset>0.2f){
//                    beginScroll=10;
//                }else if( beginScroll==2&&positionOffset<0.8f){
//                    beginScroll=20;
//                }
//                if(beginScroll==10&&positionOffset<0.001f){
//                    NewViewPager.end=1;
//                    beginScroll=0;
//                }
//                if(beginScroll==20&&positionOffset>0.99f){
//                    NewViewPager.end=1;
//                    beginScroll=0;
//                }
            }

            @Override
            public void onPageSelected(int position) {
                pos = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 2) {
                    ViewPagerAdapter adapter = (ViewPagerAdapter) getAdapter();
                    adapter.resume(pos);
                }

            }
        });
    }


    private float x,y,lastx,lasty;
    private int flagfx;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (ViewPagerFragment.mo == 0 && action == MotionEvent.ACTION_MOVE) {
            //需要移动的第一个瞬间
            if (flag == 0) {
                flag = 1;
                long time = SystemClock.uptimeMillis();//必须是 SystemClock.uptimeMillis()。
                MotionEvent downEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                super.onInterceptTouchEvent(downEvent);
                super.onTouchEvent(downEvent);
//                super.onInterceptTouchEvent(ev);
//                super.onTouchEvent(ev);
                x=ev.getX();
                y=ev.getY();
                Log.d(TAG, "onInterceptTouchEvent: "+(lastx-x));
                if(lastx-x>0){
                    flagfx=1;
                }else{
                    flagfx=2;
                }
            }
        }
        lastx=ev.getX();
        lasty=ev.getY();
        if((flagfx==1&&lastx>x)||(flagfx==2&&lastx<x)) {
            long time = SystemClock.uptimeMillis();
            MotionEvent downEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, ev.getX(), ev.getY(), 0);

            onTouchEvent(downEvent);
        }

        if(flag==1){
            //super.onInterceptTouchEvent(ev);
            onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

//        if(end==1){
//            Log.d(TAG, "onTouchEvent: endddddddddddddddddd");
//            long time = SystemClock.uptimeMillis();//必须是 SystemClock.uptimeMillis()。
//            MotionEvent downEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), 0);
//            super.onTouchEvent(downEvent);
//            super.onInterceptTouchEvent(downEvent);
//            ViewPagerFragment.mo = 1;
//            flag = 0;
//           MotionEvent  downEvent2= MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
//            onInterceptTouchEvent(downEvent2);
//            end=0;
//        }

        if (action == MotionEvent.ACTION_UP) {

            super.onTouchEvent(ev);
            super.onInterceptTouchEvent(ev);
            ViewPagerFragment.mo = 1;

            flag = 0;
            flagfx=0;
        }
        return super.onTouchEvent(ev);
    }
}
