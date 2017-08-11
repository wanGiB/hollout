package com.wan.hollout.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wan.hollout.ui.fragments.SlidePageFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wan Clem
 */
public class SlidePagerAdapter extends FragmentStatePagerAdapter {

    private List<String> picList = new ArrayList<>();

    public SlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return SlidePageFragment.newInstance(picList.get(i));
    }

    @Override
    public int getCount() {
        return picList.size();
    }

    public void addAll(List<String> picList) {
        this.picList = picList;
    }

}
