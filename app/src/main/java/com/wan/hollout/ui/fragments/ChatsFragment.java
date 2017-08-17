package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatsFragment extends Fragment {

    @BindView(R.id.chats_recycler_view)
    RecyclerView peopleRecyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.no_feed_view)
    View noFeedView;

    private List<ParseObject> chats = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View peopleView = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, peopleView);
        return peopleView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
        checkAndRegEventBus();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRegEventBus();
    }

    @Override
    public void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkAnUnRegEventBus();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAnUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String message = (String) o;
                }
            }
        });
    }

}
