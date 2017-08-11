package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;
import com.wan.hollout.interfaces.DrawerRecyclerInterface;
import com.wan.hollout.interfaces.DrawerSubmenuRecyclerInterface;
import com.wan.hollout.ui.adapters.DrawerRecyclerAdapter;
import com.wan.hollout.ui.adapters.DrawerSubmenuRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment handles the drawer menu.
 */
public class DrawerFragment extends Fragment {

    public static final int MEET_PEOPLE = 0x1;
    public static final int YOUR_PROFILE = 0x2;
    public static final int HELP_AND_SETTINGS = 0x5;
    public static final int THEME = 0x6;
    public static final int LOG_OUT = 0x7;

    /**
     * Indicates that menu is currently loading.
     */
    private boolean drawerLoading = false;

    /**
     * Listener indicating events that occurred on the menu.
     */
    private FragmentDrawerListener drawerListener;
    private DrawerLayout mDrawerLayout;

    @BindView(R.id.drawer_recycler)
    RecyclerView drawerRecycler;

    @BindView(R.id.drawer_submenu_recycler)
    RecyclerView drawerSubmenuRecycler;

    // Drawer sub menu fields
    @BindView(R.id.drawer_submenu_layout)
    LinearLayout drawerSubmenuLayout;

    @BindView(R.id.drawer_submenu_title)
    TextView drawerSubmenuTitle;

    @BindView(R.id.drawer_progress)
    ProgressBar drawerProgress;

    @BindView(R.id.drawer_retry_btn)
    Button drawerRetryBtn;

    @BindView(R.id.drawer_submenu_back_btn)
    Button backBtn;

    private DrawerSubmenuRecyclerAdapter drawerSubmenuRecyclerAdapter;
    private DrawerRecyclerAdapter drawerRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.bind(this, layout);
        drawerRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLoading)
                    getDrawerItems();
            }
        });
        prepareDrawerRecycler();
        backBtn.setOnClickListener(new View.OnClickListener() {
            private long mLastClickTime = 0;

            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                animateSubListHide();
            }
        });
        getDrawerItems();
        return layout;
    }

    /**
     * Prepare drawer menu content views, adapters and listeners.
     */
    private void prepareDrawerRecycler() {
        drawerRecyclerAdapter = new DrawerRecyclerAdapter(getContext(), new DrawerRecyclerInterface() {
            @Override
            public void onCategorySelected(View v, DrawerItemCategory drawerItemCategory) {
                if (drawerItemCategory.getChildren() == null || drawerItemCategory.getChildren().isEmpty()) {
                    if (drawerListener != null) {
                        if (drawerItemCategory.getId() == MEET_PEOPLE)
                            drawerListener.onDrawersPeopleOfSharedInterestsSelected();
                        else
                            drawerListener.onDrawerItemCategorySelected(drawerItemCategory);
                        closeDrawerMenu();
                    }
                } else
                    animateSubListShow(drawerItemCategory);
            }

            @Override
            public void onPageSelected(View v, DrawerItemPage drawerItemPage) {
                if (drawerListener != null) {
                    drawerListener.onDrawerItemPageSelected(drawerItemPage);
                    closeDrawerMenu();
                }
            }

            @Override
            public void onHeaderSelected() {
                if (drawerListener != null) {
                    drawerListener.onAccountSelected();
                    closeDrawerMenu();
                }
            }
        });
        drawerRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        drawerRecycler.setHasFixedSize(true);
        drawerRecycler.setAdapter(drawerRecyclerAdapter);

        drawerSubmenuRecyclerAdapter = new DrawerSubmenuRecyclerAdapter(new DrawerSubmenuRecyclerInterface() {
            @Override
            public void onSubCategorySelected(View v, DrawerItemCategory drawerItemCategory) {
                if (drawerListener != null) {
                    drawerListener.onDrawerItemCategorySelected(drawerItemCategory);
                    closeDrawerMenu();
                }
            }
        });
        drawerSubmenuRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        drawerSubmenuRecycler.setItemAnimator(new DefaultItemAnimator());
        drawerSubmenuRecycler.setHasFixedSize(true);
        drawerSubmenuRecycler.setAdapter(drawerSubmenuRecyclerAdapter);
    }

    /**
     * Base method for layout preparation. Also set a listener that will respond to events that occurred on the menu.
     *
     * @param drawerLayout   drawer layout, which will be managed.
     * @param eventsListener corresponding listener class.
     */
    public void setUp(DrawerLayout drawerLayout, FragmentDrawerListener eventsListener) {
        mDrawerLayout = drawerLayout;
        this.drawerListener = eventsListener;
    }

    /**
     * When the drawer menu is open, close it.
     */
    public void closeDrawerMenu() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public boolean isSubMenuVisible() {
        return drawerSubmenuLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * Method invalidates a drawer menu header. It is used primarily on a login state change.
     */
    public void invalidateHeader() {
        if (drawerRecyclerAdapter != null) {
            drawerRecyclerAdapter.notifyItemChanged(0);
        }
    }

    private void getDrawerItems() {
        drawerLoading = true;
        drawerProgress.setVisibility(View.GONE);
        drawerRetryBtn.setVisibility(View.GONE);
        drawerRecyclerAdapter.addDrawerItem(new DrawerItemCategory(MEET_PEOPLE, MEET_PEOPLE, getString(R.string.meet_people)));
        drawerRecyclerAdapter.addDrawerItem(new DrawerItemCategory(YOUR_PROFILE, YOUR_PROFILE, getString(R.string.your_profile)));
        drawerRecyclerAdapter.addDrawerItem(new DrawerItemCategory(HELP_AND_SETTINGS, HELP_AND_SETTINGS, getString(R.string.help_and_settings)));
        drawerRecyclerAdapter.addDrawerItem(new DrawerItemCategory(THEME, THEME, getString(R.string.theme)));
        drawerRecyclerAdapter.addDrawerItem(new DrawerItemCategory(LOG_OUT, LOG_OUT, getString(R.string.log_out)));
        drawerRecyclerAdapter.notifyDataSetChanged();
        drawerLoading = false;
    }

    public void animateSubListHide() {
        Animation slideAwayDisappear = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_away_disappear);
        final Animation slideAwayAppear = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_away_appear);
        slideAwayDisappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                drawerRecycler.setVisibility(View.VISIBLE);
                drawerRecycler.startAnimation(slideAwayAppear);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                drawerSubmenuLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        drawerSubmenuLayout.startAnimation(slideAwayDisappear);
    }

    private void animateSubListShow(DrawerItemCategory drawerItemCategory) {
        if (drawerItemCategory != null) {
            drawerSubmenuTitle.setText(drawerItemCategory.getName());
            drawerSubmenuRecyclerAdapter.changeDrawerItems(drawerItemCategory.getChildren());
            Animation slideInDisappear = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_disappear);
            final Animation slideInAppear = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_appear);
            slideInDisappear.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    drawerSubmenuLayout.setVisibility(View.VISIBLE);
                    drawerSubmenuLayout.startAnimation(slideInAppear);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    drawerRecycler.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            drawerRecycler.startAnimation(slideInDisappear);
        }
    }

    @Override
    public void onPause() {
        // Cancellation during onPause is needed because of app restarting during changing shop.
        if (drawerLoading) {
            if (drawerProgress != null) drawerProgress.setVisibility(View.GONE);
            if (drawerRetryBtn != null) drawerRetryBtn.setVisibility(View.VISIBLE);
            drawerLoading = false;
        }
        super.onPause();
    }

    /**
     * Interface defining events initiated by {@link DrawerFragment}.
     */
    public interface FragmentDrawerListener {
        void onDrawersPeopleOfSharedInterestsSelected();

        void onDrawerItemCategorySelected(DrawerItemCategory drawerItemCategory);

        void onDrawerItemPageSelected(DrawerItemPage drawerItemPage);

        void onAccountSelected();
    }

}
