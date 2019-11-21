package com.demo.tikpic;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class ViewPagerActivity extends FragmentActivity {

    private static final String TAG = "ViewPagerActivity";
    // private static final String TAG = "VideoFragment";

    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
    private List<String> mediaPaths;
    private Fragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        mediaPaths = DataManager.getInstance(this).getImagePaths();

        mPager = findViewById(R.id.pager);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(getIntent().getIntExtra("position", 0));


        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                String uri = mediaPaths.get(position);
                String id = uri.substring(uri.lastIndexOf('/'));

                Log.d(TAG, "onPageSelected: current page " + id);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE) {

                }
                else if(state == ViewPager.SCROLL_STATE_DRAGGING) {
                }
                else if(state == ViewPager.SCROLL_STATE_SETTLING) {
                }
            }
        });
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: position " + position);
            Log.d(TAG, "getItem: mPager.getCurrentItem() " + mPager.getCurrentItem());
            String uri = mediaPaths.get(position);

            if(uri.contains("content://media/external/video")) {
                Fragment fragment = VideoFragment.newInstance(uri);
                return fragment;
            }
            else {
                return ImageFragment.newInstance(uri);
            }

        }

        @Override
        public int getCount() { return mediaPaths.size(); }
    }
}
