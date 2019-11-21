package com.demo.tikpic.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.tikpic.MainActivity;

public class ViewPagerFragment extends Fragment {

    private static String TAG = "ViewPagerFragment";
    private Context mContext;
    private MainActivity mActivity;

    private NewViewPager view;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().hide();
        }
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        // Log.d(TAG, "onCreateView: " + DataManager.getInstance(mActivity).getShowcaseOrAlbumOrIndex(0).size());

        view = new NewViewPager(mContext,mActivity);
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(mContext,mActivity,0,0);
        view.setAdapter(mAdapter);
        view.setCurrentItem(mPicIndex);
        return view;

      /* VideoDisplayView view = new VideoDisplayView(mContext);
       view.setResourse(mActivity.data.get(mListIndex,mPicIndex).getPath());
       return view;*/

    }

}
