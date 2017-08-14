package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.utils.FontUtils;

import java.util.ArrayList;

/**
 * @author Wan Clem
 */
public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;
    private LayoutInflater layoutInflater;
    private Context context;

    public MainViewPagerAdapter(Context context, FragmentManager fm, ArrayList<Fragment> fragments, ArrayList<String> titles) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
        this.titles = titles;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public View getCustomTabView(int pos) {
        @SuppressWarnings("InflateParams")
        View view = layoutInflater.inflate(R.layout.tab_custom_view, null);
        TextView tabTitle = view.findViewById(R.id.tab_title);
        Typeface typeface = FontUtils.selectTypeface(context, 1);
        tabTitle.setTypeface(typeface);
        tabTitle.setText(getPageTitle(pos));
        return view;
    }

}
