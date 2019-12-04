package com.demo.tikpic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.demo.tikpic.R;

public class VideoToolbar extends RelativeLayout {
    public VideoToolbar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.video_toolbar,this);
    }

    public VideoToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.video_toolbar,this);
    }

    public VideoToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
