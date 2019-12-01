package com.demo.tikpic.viewpager;

import android.content.Context;
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

    //  private static String TAG = "ViewPagerFragment";
    private Context mContext;
    private MainActivity mActivity;

    static public int mo = 1;

    private int gallryIndex = -1, albumIndex = -1, itemIndex = -1;

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

        if (gallryIndex == -1 && albumIndex == -1 && itemIndex == -1) {
            Toast.makeText(getContext(), "没有坐标", Toast.LENGTH_LONG).show();
            return null;
        } else {
            NewViewPager view = new NewViewPager(mContext);
            List<MediaFile> list = DataManager.getInstance().getShowcaseOrAlbumOrIndex(gallryIndex, albumIndex);
            ViewPagerAdapter mAdapter = new ViewPagerAdapter(mContext, list);
            view.setAdapter(mAdapter);
            view.setCurrentItem(itemIndex);
            return view;
        }

    }

    public void setPosition(int gallryIndex, int albumIndex, int itemIndex) {
        this.gallryIndex = gallryIndex;
        this.albumIndex = albumIndex;
        this.itemIndex = itemIndex;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().show();
        }
    }
}
