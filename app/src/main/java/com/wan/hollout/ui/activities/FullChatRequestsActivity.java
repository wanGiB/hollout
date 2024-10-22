package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.ChatRequestNegotiationResult;
import com.wan.hollout.interfaces.EndlessRecyclerViewScrollListener;
import com.wan.hollout.ui.adapters.ChatRequestsAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
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
    ProgressBar progressWheel;

    @BindView(R.id.chat_requests_recycler_view)
    RecyclerView chatRequestsRecyclerView;

    @BindView(R.id.instruction_header)
    TextView instructionHeader;

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
        instructionHeader.setText(UiUtils.fromHtml("Swipe left and right to accept/decline"));
        initFeedAdapter();
        fetchChatRequests(0);
        checkAndRegEventBus();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
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
        chatRequestsAdapter.unregisterAdapterDataObserver(adapterDataObserver);
    }

    private void initFeedAdapter() {
        chatRequestsAdapter = new ChatRequestsAdapter(this, chatRequests);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        chatRequestsRecyclerView.setLayoutManager(linearLayoutManager);
        chatRequestsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
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

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    private void fetchChatRequests(final int skip) {
        if (signedInUser != null) {
            final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.setLimit(30);
            if (skip != 0) {
                chatRequestsQuery.setSkip(skip);
            }
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
            chatRequestsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (skip == 0) {
                        chatRequests.clear();
                    }
                    if (e == null && objects != null && !objects.isEmpty()) {
                        loadAdapter(objects);
                    }
                    chatRequestsQuery.cancel();
                }
            });
        }
    }

    private void loadAdapter(List<ParseObject> objects) {
        final List<ParseObject> deletables = new ArrayList<>();
        for (ParseObject parseObject : objects) {
            ParseObject feedCreator = parseObject.getParseObject(AppConstants.FEED_CREATOR);
            if (feedCreator != null) {
                String userAppDisplayName = feedCreator.getString(AppConstants.APP_USER_DISPLAY_NAME);
                if (StringUtils.isNotEmpty(userAppDisplayName)) {
                    if (!chatRequests.contains(parseObject)) {
                        chatRequests.add(parseObject);
                    }
                } else {
                    deletables.add(parseObject);
                }
            }else {
                deletables.add(parseObject);
            }
        }
        chatRequestsAdapter.notifyDataSetChanged();
        if (!chatRequests.isEmpty()) {
            hideEmptyViewsAndShowRecyclerView();
        }
        if (!deletables.isEmpty()) {
            ParseObject.deleteAllInBackground(deletables, new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (chatRequests.containsAll(deletables)) {
                        chatRequests.removeAll(deletables);
                    }
                    deletables.clear();
                }
            });
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
                if (o instanceof ChatRequestNegotiationResult) {
                    ChatRequestNegotiationResult chatRequestNegotiationResult = (ChatRequestNegotiationResult) o;
                    ParseObject chatRequest = chatRequestNegotiationResult.getChatRequest();
                    if (chatRequest != null) {
                        if (chatRequestNegotiationResult.canRemove()) {
                            if (chatRequests.contains(chatRequest)) {
                                chatRequests.remove(chatRequestNegotiationResult.getPosition());
                                chatRequestsAdapter.notifyItemRemoved(chatRequestNegotiationResult.getPosition());
                                chatRequestsAdapter.notifyDataSetChanged();
                            }
                        } else {
                            chatRequests.add(chatRequestNegotiationResult.getPosition(), chatRequest);
                            chatRequestsAdapter.notifyItemInserted(chatRequestNegotiationResult.getPosition());
                            chatRequestsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

}

