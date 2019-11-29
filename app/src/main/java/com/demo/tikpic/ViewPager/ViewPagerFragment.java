package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.tikpic.DataManager;
import com.demo.tikpic.MainActivity;
import com.demo.tikpic.itemClass.MediaFile;

import java.util.List;

public class ViewPagerFragment extends Fragment {

    private static String TAG = "ViewPagerFragment";
    private Context mContext;
    private MainActivity mActivity;

    private NewViewPager view;

    private int gallryIndex=-1,albumIndex=-1,itemIndex=-1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().hide();
        }
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        // Log.d(TAG, "onCreateView: " + DataManager.getInstance(mActivity).getShowcaseOrAlbumOrIndex(0).size());
        if(gallryIndex==-1&&albumIndex==-1&&itemIndex==-1){
            Toast.makeText(getContext(),"没有坐标",Toast.LENGTH_LONG).show();
            return null;
        }else{
            view = new NewViewPager(mContext);
            List<MediaFile> list = DataManager.getInstance().getShowcaseOrAlbumOrIndex(gallryIndex,albumIndex);
            ViewPagerAdapter mAdapter = new ViewPagerAdapter(mContext,list);
            view.setAdapter(mAdapter);
            view.setCurrentItem(itemIndex);
            return view;
        }


      /* VideoDisplayView view = new VideoDisplayView(mContext);
       view.setResourse(mActivity.data.get(mListIndex,mPicIndex).getPath());
       return view;*/

    }

    public void setPosition(int gallryIndex,int albumIndex,int itemIndex){
        this.gallryIndex=gallryIndex;
        this.albumIndex=albumIndex;
        this.itemIndex=itemIndex;
    }

}
