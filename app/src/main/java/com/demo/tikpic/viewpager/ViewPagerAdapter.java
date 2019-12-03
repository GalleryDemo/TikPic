package com.demo.tikpic.viewpager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.demo.tikpic.itemClass.MediaFile;
import com.demo.tikpic.view.ImageDisplayView;
import com.demo.tikpic.view.VideoView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter {

    private String TAG = "MyPagerAdapter";
    private List<MediaFile> mList;
    private Context mContext;
    private Map<Integer, View> mViewMap;

    ViewPagerAdapter(Context context, List list) {
        mContext = context;
        mList = list;
        mViewMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MediaFile item = mList.get(position);

        if (item.getType() == 1) {
            ImageDisplayView view = new ImageDisplayView(mContext);
            view.setUri(Uri.parse(item.getPath()));
            container.addView(view);
            mViewMap.put(position, view);
            return view;
        } else {
            VideoView view = new VideoView(mContext, item.getPath());
            container.addView(view);
            mViewMap.put(position, view);
            return view;
        }
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
        MediaFile item = mList.get(position);

        //图片单独销毁
        if (item.getType() == 1) {
            ImageDisplayView view = (ImageDisplayView) mViewMap.get(position);
            view.destroy();
        }
        container.removeView(mViewMap.get(position));
        mViewMap.remove(position);
    }

    public void resume(int position) {
        Log.d(TAG, "resume: " + position);
        if (mList.get(position).getType() == 1) {
            ImageDisplayView view = (ImageDisplayView) mViewMap.get(position);
            if (view != null) {
                view.reset();
            }
        } else {
            VideoView view = (VideoView) mViewMap.get(position);
            if (view != null) {
                view.reset();
            }
        }
    }
}
