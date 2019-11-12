package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.demo.tikpic.MainActivity;
import com.demo.tikpic.itemClass.MediaFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter {

    private String TAG  = "MyPagerAdapter";
    private MainActivity mActivity;
    private List<Integer> mList;
    private int listIndex;
    private int imgIndex;
    private Context mContext;
    private Map<Integer, View> mViewMap;

    public ViewPagerAdapter(Context context, MainActivity activity_main, int listIndex, int imgIndex) {
        mContext = context;
        mActivity = activity_main;
        this.listIndex = listIndex;
        mList = mActivity.data.getShowcaseOrAlbumOrIndex(0,listIndex);
        Log.d(TAG, "ViewPagerAdapter: "+mList.size());
        this.imgIndex = imgIndex;
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

        MediaFile item = mActivity.data.getShowcaseOrAlbumOrIndex(0,0,0);
        if(item.getType()==1){
            Log.d(TAG, "instantiateItem: "+position);

            ImageDisplayView view = new ImageDisplayView(mContext, mActivity);
            view.setAlbumImage(position, listIndex);

            container.addView(view);
            mViewMap.put(position,view);
            return view;

        }else{
            /*VideoDisplayView view = new VideoDisplayView(mContext);
            view.setResourse(item.getPath());
            container.addView(view);
            mViewMap.put(position,view);
            return view;*/
            return new ImageView(mContext);

        }

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);

        container.removeView(mViewMap.get(position));
    }
}
