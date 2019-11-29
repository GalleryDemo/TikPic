package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.ImageFragment;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.NewVideoFragment;
import com.demo.tikpic.itemClass.MediaFile;

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
        container.removeView(mViewMap.get(position));
    }
}
