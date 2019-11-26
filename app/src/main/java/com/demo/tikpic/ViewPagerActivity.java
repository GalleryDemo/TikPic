package com.demo.tikpic;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class ViewPagerActivity extends FragmentActivity {

    private static final String TAG = "ViewPagerActivity";
    // private static final String TAG = "VideoFragment";

    private ViewPager mPager;
    private ViewPagerAdapter pagerAdapter;
    private List<String> mediaPaths;

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

                Log.d("TEST", "onPageSelected: current page " + id);
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

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentArray = new SparseArray<>(getCount());
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            String uri = mediaPaths.get(position);

            if(uri.contains("content://media/external/video")) {
                // Fragment fragment = VideoFragment.newInstance(uri);
                Fragment fragment = NewVideoFragment.newInstance(uri);
                fragmentArray.put(position, fragment);
                return fragment;
            }
            else {
                Fragment fragment = ImageFragment.newInstance(uri);
                fragmentArray.put(position, fragment);
                return fragment;
            }
        }

        @Override
        public int getCount() { return mediaPaths.size(); }

        public Fragment getFragment(int position) {
            return fragmentArray.get(position);
        }

        public int getFragmentCount() {
            return fragmentArray.size();
        }
    }
}
