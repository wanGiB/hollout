package com.wan.hollout.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.app.hollout.R;
import com.app.hollout.utils.FontUtils;

import java.util.List;

/**
 * @author Wan Clem
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private AppCompatActivity appCompatActivity;
    private List<Fragment> fragments;
    private List<String> tabTitles;

    public ViewPagerAdapter(FragmentManager fm, AppCompatActivity appCompatActivity, List<Fragment> fragments, List<String> tabTitles) {
        super(fm);
        this.appCompatActivity = appCompatActivity;
        this.fragments = fragments;
        this.tabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    public View getCustomTabView(int pos) {
        @SuppressWarnings("InflateParams")
        View view = appCompatActivity.getLayoutInflater().inflate(R.layout.tab_custom_view, null);
        TextView tabTitle = (TextView) view.findViewById(R.id.tab_title);
        tabTitle.setTypeface(FontUtils.getTypeface(appCompatActivity, "Roboto-Bold.ttf"));
        tabTitle.setText(getPageTitle(pos));
        return view;
    }

}
