package com.demo.tikpic.ViewPager;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import androidx.core.view.MotionEventCompat;

public class InputDetector {

    private OnInputListener mListener;
    private int state = 0;
    private int[][] stateTable;

    public float x, y;

    private float mLastX, mLastY;
    public float dx, dy;
    public double distance, lastDist;
    public Point middle;
    public int gestureX, gestureY;
    private int num_h;

    public int mo=0;

    public boolean in67;

    private String TAG = "InputDetector";

    public interface OnInputListener {
        boolean action(int state);
    }

    public InputDetector(OnInputListener linstener) {
        mListener = linstener;
        stateTable = new int[][]{{1, 0, 0, 0, 0}, {0, 4, 3, 2, 0}, {0, 0, 2, 2, 3}, {0, 0, 3, 2, 0}};
    }

    public boolean onTouchEvent(MotionEvent e) {

        stateJump(e);
        Log.d(TAG, "onTouchEvent: "+ MotionEventCompat.getActionMasked(e)+"//"+mo);
       return true;
    }

    //状态跳转表
    public void stateJump(MotionEvent event) {

        x = event.getX();
        y = event.getY();

        if(num_h>1){

            distance = distance(event);
            middle = middle(event);
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            // 单指
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;

                gestureX = 0;
                gestureY = 0;

                num_h = 1;

                in67=true;

                state = stateTable[state][0];
                break;
            case MotionEvent.ACTION_UP:
                num_h = 0;
                state = stateTable[state][1];
                break;
            case MotionEvent.ACTION_MOVE:
                dx = x - mLastX;
                dy = y - mLastY;

                gestureY += dy;
                gestureX += dx;


                if(num_h>1){

                    distance = distance(event);
                    middle = middle(event);
                }



                //移动了距离才算移动
                if (Math.abs(dx) >= 1 || Math.abs(dy) >= 1) {
                    state = stateTable[state][2];
                }
                if (Math.abs(gestureX) < 300&&in67) {
                    if (gestureY > 400) {
                        state = 6;
                    } else if (gestureY < -400) {
                        state = 7;
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                in67=false;
                num_h++;
                state = stateTable[state][3];
                distance = distance(event);
                lastDist=distance;
                middle = middle(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                num_h--;

                state = stateTable[state][4];
                break;
            default:
                break;
        }
        stateProcess();

    }

    //状态处理表
    public void stateProcess() {

        mListener.action(state);

        //对于不需要再处理操作的状态直接置0
        switch (state) {
            case 4:
                state = 0;
                break;
            case 6:
                in67=false;
                state = 3;
                break;
            case 7:
                in67=false;
                state = 3;
                break;
        }
    }


    // 计算两个触摸点之间的距离
    private Double distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    // 计算两个触摸点的中点
    private Point middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new Point((int) x / 2, (int) y / 2);
    }

}
