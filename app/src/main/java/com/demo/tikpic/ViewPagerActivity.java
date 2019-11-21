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

    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
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
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            String uri = mediaPaths.get(position);
            if(uri.contains("content://media/external/video")) {
                Log.d(TAG, "getItem: return VideoFragment.newInstance");
                return VideoFragment.newInstance(uri);
            }
            else {
                return ImageFragment.newInstance(uri);
            }

        }

        @Override
        public int getCount() { return mediaPaths.size(); }
    }
}
