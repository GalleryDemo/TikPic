package com.demo.tikpic;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.demo.tikpic.itemClass.MediaFile;

import java.util.List;

public class ViewPagerActivity extends FragmentActivity {

    private static final String TAG = "ViewPagerActivity";
    // private static final String TAG = "VideoFragment";

    private ViewPager mPager;
    private ViewPagerAdapter pagerAdapter;
    private List<String> mediaPaths;
    private List<MediaFile> allFiles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        mediaPaths = DataManager.getInstance(this).getImagePaths();
        allFiles = DataManager.getInstance(this).getAllItemList();


        mPager = findViewById(R.id.pager);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        int a = getIntent().getIntExtra("position", 0);
        Log.d(TAG, "onCreate - A: " + a );
        mPager.setCurrentItem(a);


        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onCreate: FINISHED " + position);
                String uri = allFiles.get(position).getPath();
                String id = uri.substring(uri.lastIndexOf('/'));

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE) {
                }
                else if(state == ViewPager.SCROLL_STATE_DRAGGING) {
                    Fragment fragment = pagerAdapter.getFragment(mPager.getCurrentItem());
                    if(fragment instanceof NewVideoFragment) {
                        ((NewVideoFragment) fragment).pauseVideo();
                        ((NewVideoFragment) fragment).stopSeekBarSyncThread();
                    }
                }
                else if(state == ViewPager.SCROLL_STATE_SETTLING) {
                }
            }
        });
        Log.d(TAG, "onCreate: A " +a);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        Log.d(TAG, "onCreate: B ");
        return super.onCreateView(name, context, attrs);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment fragment = pagerAdapter.getFragment(mPager.getCurrentItem());
        if(fragment instanceof NewVideoFragment) {
            ((NewVideoFragment) fragment).stopSeekBarSyncThread();
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<Fragment> fragmentArray;
        private static final String TAG = "GestureViewPagerAdapter";

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentArray = new SparseArray<>(getCount());
            Log.d(TAG, "GestureViewPagerAdapter: " + allFiles.size());
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Log.d(TAG, "instantiateItem: " + position);
            return super.instantiateItem(container, position);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: " + position);
            MediaFile file = allFiles.get(position);
            if(file.getType() == 3) {
                // Fragment fragment = VideoFragment.newInstance(uri);
                Fragment fragment = NewVideoFragment.newInstance(file.getPath());
                fragmentArray.put(position, fragment);
                return fragment;
            }
            else {
                Fragment fragment = ImageFragment.newInstance(file.getPath());

                fragmentArray.put(position, fragment);
                return fragment;
            }
        }

        @Override
        public int getCount() { return allFiles.size(); }

        public Fragment getFragment(int position) {
            return fragmentArray.get(position);
        }

        public int getFragmentCount() {
            return fragmentArray.size();
        }
    }
}
