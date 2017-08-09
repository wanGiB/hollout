package com.wan.hollout.ui.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.wan.hollout.R;
import com.wan.hollout.utils.ATEUtils;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.HolloutPreferences;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_main, container, false);

        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (viewPager != null) {
            Adapter adapter = setupViewPagerAdapter(viewPager);
            viewPager.setOffscreenPageLimit(2);
            tabLayout.setSelectedTabIndicatorHeight(6);
            tabLayout.setupWithViewPager(viewPager);
            setupTabs(adapter);
        }
        return rootView;
    }

    private void setupTabs(Adapter pagerAdapter) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getCustomTabView(i));
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }
        viewPager.setCurrentItem(HolloutPreferences.getStartPageIndex());
    }

    private Adapter setupViewPagerAdapter(ViewPager viewPager) {
        Adapter adapter = new Adapter(getActivity(), getChildFragmentManager());
        adapter.addFragment(new FeedFragment(), this.getString(R.string.feed));
        adapter.addFragment(new PeopleFragment(), this.getString(R.string.people));
        viewPager.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onPause() {
        super.onPause();
        HolloutPreferences.setStartPageIndex(viewPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = HolloutPreferences.getATEKey();
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();
        private LayoutInflater layoutInflater;
        private Context context;

        Adapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        View getCustomTabView(int pos) {
            @SuppressWarnings("InflateParams")
            View view = layoutInflater.inflate(R.layout.tab_custom_view, null);
            TextView tabTitle = view.findViewById(R.id.tab_title);
            Typeface typeface = FontUtils.selectTypeface(context, 1);
            tabTitle.setTypeface(typeface);
            tabTitle.setText(getPageTitle(pos));
            return view;
        }

    }


}
