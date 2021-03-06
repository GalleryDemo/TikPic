package com.demo.tikpic.viewpager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.DragAndDropPermissions;
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
import com.demo.tikpic.R;
import com.demo.tikpic.itemClass.MediaFile;
import com.demo.tikpic.view.ImageDisplayView;
import com.demo.tikpic.view.VideoView;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewPagerFragment extends Fragment {

    private static String TAG = "ViewPagerFragment";
    private Context mContext;
    private MainActivity mActivity;
    private GestureViewPager view;
    private int gallryIndex = -1, albumIndex = -1, itemIndex = -1;

    boolean flag = true;

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

            if (flag) {
                view = new GestureViewPager(mContext);
                view.setBackgroundColor(getResources().getColor(R.color.black));
                view.setPageMargin((int) getResources().getDimensionPixelOffset(R.dimen.page_margen));
                List<MediaFile> list = DataManager.getInstance().getShowcaseOrAlbumOrIndex(gallryIndex, albumIndex);
                GestureViewPagerAdapter mAdapter = new GestureViewPagerAdapter(mContext, list);
                view.setAdapter(mAdapter);
                view.setCurrentItem(itemIndex);
                return view;
            } else {
                //单图片界面调试
                List<MediaFile> list = DataManager.getInstance().getShowcaseOrAlbumOrIndex(gallryIndex, albumIndex);
                MediaFile item = list.get(itemIndex);

                ImageDisplayView view = new ImageDisplayView(mContext);
                view.setUri(Uri.parse(item.getPath()));
                return view;
            }
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
        if (flag) {
            Log.d(TAG, "onDestroyView: Item: " + view.getCurrentItem());

            DataManager.getInstance().setCurrenPosition(view.getCurrentItem());
        }
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().show();
        }

    }


}
