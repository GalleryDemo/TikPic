package com.demo.tikpic;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerFragment extends Fragment {

    private static String TAG = "Fragment_Albums";
    private Context mContext;
    private MainActivity mActivity;
    private ViewPager view;

    private int mListIndex, mPicIndex;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (MainActivity) getActivity();

        mListIndex = mActivity.pos[1];
        mPicIndex = mActivity.pos[2];
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().hide();
        }
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏



        view = new ViewPager(mContext);
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(mContext,mActivity,mListIndex,mPicIndex);
        view.setAdapter(mAdapter);
        view.setCurrentItem(mPicIndex);
        return view;

      /* VideoDisplayView view = new VideoDisplayView(mContext);
       view.setResourse(mActivity.data.get(mListIndex,mPicIndex).getPath());
       return view;*/

    }

}
