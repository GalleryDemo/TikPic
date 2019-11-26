package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.itemClass.MediaFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter {

    private String TAG  = "MyPagerAdapter";
    private MainActivity mActivity;
    // private List<MediaFile> mList;

    private List<String> mediaPaths;

    private int listIndex;
    private int imgIndex;
    private Context mContext;
    private Map<Integer, View> mViewMap;

    ViewPagerAdapter(Context context, MainActivity mainActivity, int listIndex, int imgIndex) {
        mContext = context;
        mActivity = mainActivity;
        this.listIndex = listIndex;

        // mList = DataManager.getInstance(mActivity).getShowcaseOrAlbumOrIndex(0,listIndex);
        // Log.d(TAG, "ViewPagerAdapter: "+mList.size());

        mediaPaths = DataManager.getInstance(mainActivity).getImagePaths();

        this.imgIndex = imgIndex;
        mViewMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mediaPaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        MediaFile item = DataManager.getInstance(mActivity).getShowcaseOrAlbumOrIndex(0,0,0);

        if(item.getType() == 1) {
            Log.d(TAG, "instantiateItem: " + position);

            ImageDisplayView view = new ImageDisplayView(mContext, mActivity);
            view.setAlbumImage(position, listIndex);

            container.addView(view);
            mViewMap.put(position,view);
            return view;
        }
        else {
            VideoView view = new VideoView(mContext, DataManager.getInstance(mActivity).getShowcaseOrAlbumOrIndex(0,listIndex, position).getPath());
            container.addView(view);
            mViewMap.put(position,view);
            return view;
        }
    }



    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);

        container.removeView(mViewMap.get(position));
    }
}
