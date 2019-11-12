package com.demo.tikpic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerFragment extends Fragment {

    private MainActivity hostActivity;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        hostActivity = (MainActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_view_pager, container, false);
        viewPager = rootView.findViewById(R.id.pager);
        pagerAdapter = new PhotoSlidePagerAdapter(hostActivity.getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // TODO: GET CURRENT ITEM FROM HOST ACTIVITY
        // viewPager.setCurrentItem(0);

        return rootView;
    }

    private class PhotoSlidePagerAdapter extends FragmentStatePagerAdapter {

        PhotoSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() { return 0; }
    }

}
