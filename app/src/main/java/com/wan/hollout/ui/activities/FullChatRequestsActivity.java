package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.EndlessRecyclerViewScrollListener;
import com.wan.hollout.eventbuses.RemovableChatRequestEvent;
import com.wan.hollout.ui.adapters.ChatRequestsAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullChatRequestsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nothing_to_load)
    HolloutTextView nothingToLoadView;

    @BindView(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @BindView(R.id.chat_requests_recycler_view)
    RecyclerView chatRequestsRecyclerView;

    private ParseObject signedInUser;
    private List<ParseObject> chatRequests = new ArrayList<>();
    private ChatRequestsAdapter chatRequestsAdapter;

    RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            if (chatRequestsAdapter.getChatRequests().isEmpty()) {
                finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_chat_requests);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chat Requests");
        }
        signedInUser = AuthUtil.getCurrentUser();
        initFeedAdapter();
        fetchChatRequests(0);
        checkAndRegEventBus();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);

        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        MenuItem createNewGroupItem = menu.findItem(R.id.create_new_group);

        createNewGroupItem.setVisible(false);
        filterPeopleMenuItem.setVisible(false);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
        if (chatRequestsAdapter != null) {
            chatRequestsAdapter.registerAdapterDataObserver(adapterDataObserver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
        chatRequestsAdapter.unregisterAdapterDataObserver(adapterDataObserver);
    }

    private void initFeedAdapter() {
        chatRequestsAdapter = new ChatRequestsAdapter(this, chatRequests);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        chatRequestsRecyclerView.setLayoutManager(linearLayoutManager);
        chatRequestsRecyclerView.setAdapter(chatRequestsAdapter);
        chatRequestsRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!chatRequests.isEmpty()) {
                    fetchChatRequests(chatRequests.size());
                }
            }
        });
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

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    private void fetchChatRequests(final int skip) {
        if (signedInUser != null) {
            ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.setLimit(30);
            if (skip != 0) {
                chatRequestsQuery.setSkip(skip);
            }
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            chatRequestsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (skip == 0) {
                        chatRequests.clear();
                    }
                    if (e == null && objects != null && !objects.isEmpty()) {
                        loadAdapter(objects);
                    }
                }
            });
        }
    }

    private void loadAdapter(List<ParseObject> objects) {
        for (ParseObject parseObject : objects) {
            if (!chatRequests.contains(parseObject)) {
                chatRequests.add(parseObject);
            }
        }
        chatRequestsAdapter.notifyDataSetChanged();
        if (!chatRequests.isEmpty()) {
            hideEmptyViewsAndShowRecyclerView();
        }

    }

    private void hideEmptyViewsAndShowRecyclerView() {
        UiUtils.showView(nothingToLoadView, false);
        UiUtils.showView(progressWheel, false);
        UiUtils.showView(chatRequestsRecyclerView, true);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof RemovableChatRequestEvent) {
                    RemovableChatRequestEvent removableChatRequestEvent = (RemovableChatRequestEvent) o;
                    ParseObject removableChatRequest = removableChatRequestEvent.getRemovableChatRequest();
                    if (removableChatRequest != null) {
                        if (chatRequests.contains(removableChatRequest)) {
                            chatRequests.remove(removableChatRequest);
                            chatRequestsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

}

