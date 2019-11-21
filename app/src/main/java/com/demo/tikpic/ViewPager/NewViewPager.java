package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.demo.tikpic.MainActivity;

public class NewViewPager extends ViewPager {
    private MainActivity mActivity;
    String TAG = "NewViewPager";
    int flag = 0;

    public NewViewPager(@NonNull Context context) {
        super(context);
    }

    public NewViewPager(Context context, MainActivity activity) {
        super(context);
        mActivity = activity;
    }

    public NewViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;


/*
        Log.d(TAG, "onInterceptTouchEvent: mo" + mActivity.mo);
        if (mActivity.mo == 0) {
            super.onTouchEvent(ev);
            flag = 1;
        } else {
        }*/

       // Log.d(TAG, "onInterceptTouchEvent: mo"+mActivity.mo +" flag :"+flag);
        if(action == MotionEvent.ACTION_DOWN){
            super.onTouchEvent(ev);
            super.onInterceptTouchEvent(ev);
        }
        if (action == MotionEvent.ACTION_UP && flag == 1) {
            super.onTouchEvent(ev);
            super.onInterceptTouchEvent(ev);
            //mActivity.mo = 1;
            flag = 0;
        }
    /*    if (mActivity.mo == 0||flag==1) {
            super.onTouchEvent(ev);
            super.onInterceptTouchEvent(ev);

            flag=1;
        }*/
        return false;
    }
}
