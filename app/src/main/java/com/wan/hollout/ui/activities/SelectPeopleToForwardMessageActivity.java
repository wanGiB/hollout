package com.wan.hollout.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.interfaces.EndlessRecyclerViewScrollListener;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.adapters.SelectPeopleAdapter;
import com.wan.hollout.ui.widgets.MaterialSearchView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class SelectPeopleToForwardMessageActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.people_recycler_view)
    RecyclerView peopleRecyclerView;

    @BindView(R.id.done_fab)
    FloatingActionButton doneFab;

    @BindView(R.id.progress_bar)
    ProgressBar progressWheel;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    private List<ConversationItem> people = new ArrayList<>();

    private SelectPeopleAdapter selectPeopleAdapter;

    private ParseObject signedInUser;

    private String from;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_people);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Forward Message to:");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        signedInUser = AuthUtil.getCurrentUser();
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            from = intentExtras.getString(AppConstants.EXTRA_FROM);
        }
        doneFab.hide();
        setupAdapter();
        fetchConversations(0);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                people.clear();
                searchChats(people.size(), newText);
                return true;
            }
        });
        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        int peopleCount = AppConstants.selectedPeople.size();
        ProgressDialog progressDialog = UiUtils.showProgressDialog(this, "Forwarding Message to (" + (peopleCount == 1 ? "1 person)" : peopleCount + "persons)") + ")");
        ChatMessage message = AppConstants.selectedMessages.get(0);
        for (ConversationItem conversationItem : AppConstants.selectedPeople) {
            sendNewMessage(message, conversationItem.getRecipient());
        }
        UiUtils.dismissProgressDialog(progressDialog);
        UiUtils.showSafeToast("Message Forwarded!");
        AppConstants.selectedPeople.clear();
        AppConstants.selectedPeoplePositions.clear();
        //Clear selected message queue
        Intent resultIntent = getIntent();
        resultIntent.putExtra(AppConstants.FORWARDED_MESSAGE_RESULT, AppConstants.RESULT_OK);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    protected void sendNewMessage(ChatMessage message, ParseObject recipientProps) {
        if (signedInUser != null) {
            message.setTo(recipientProps.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
            String signedInUserDisplayName = signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String signedInUserPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            message.setFromName(signedInUserDisplayName);
            if (StringUtils.isNotEmpty(signedInUserPhotoUrl)) {
                message.setFromPhotoUrl(signedInUserPhotoUrl);
            }
            message.setTo(recipientProps.getString(AppConstants.REAL_OBJECT_ID));
            message.setConversationId(recipientProps.getString(AppConstants.REAL_OBJECT_ID));
            message.setFrom(signedInUserId.toLowerCase());
            message.setTimeStamp(System.currentTimeMillis());
            message.setMessageId(RandomStringUtils.random(6, true, true) + System.currentTimeMillis());
            ChatClient.getInstance().sendMessage(message, recipientProps);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem filterPeopleItem = menu.findItem(R.id.filter_people);
        filterPeopleItem.setVisible(false);

//        MenuItem createNewGroupItem = menu.findItem(R.id.create_new_group);
//        createNewGroupItem.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView.setMenuItem(searchItem);

        supportInvalidateOptionsMenu();

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (!AppConstants.selectedPeople.isEmpty()) {
            AppConstants.selectedPeople.clear();
            AppConstants.selectedPeoplePositions.clear();
            selectPeopleAdapter.notifyDataSetChanged();
            getSupportActionBar().setTitle("Forward Message to:");
            doneFab.hide();
            return;
        }
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            return;
        }
        super.onBackPressed();
    }

    private void setupAdapter() {
        selectPeopleAdapter = new SelectPeopleAdapter(this, people);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        peopleRecyclerView.setLayoutManager(linearLayoutManager);
        peopleRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        peopleRecyclerView.setAdapter(selectPeopleAdapter);
        peopleRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!people.isEmpty()) {
                    fetchConversations(people.size());
                }
            }
        });
    }

    private void fetchConversations(final int skip) {
        final ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
            if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
                peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
                List<String> exclusions = new ArrayList<>();
                String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
                if (!exclusions.contains(signedInUserId)) {
                    exclusions.add(signedInUserId);
                }
                if (!exclusions.contains(from)) {
                    exclusions.add(from);
                }
                peopleAndGroupsQuery.whereNotContainedIn(AppConstants.REAL_OBJECT_ID, exclusions);
                peopleAndGroupsQuery.setLimit(100);
                if (skip != 0) {
                    peopleAndGroupsQuery.setSkip(skip);
                }
                peopleAndGroupsQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (objects != null && !objects.isEmpty()) {
                                if (skip == 0) {
                                    people.clear();
                                }
                                sortConversations();
                                selectPeopleAdapter.setSearchString(null);
                                loadAdapter(objects);
                            }
                        }
                        peopleAndGroupsQuery.cancel();
                    }
                });
            }
        }
    }

    private void searchChats(final int skip, final String searchString) {
        selectPeopleAdapter.setSearchString(searchString);
        final ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
            if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
                peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
                List<String> exclusions = new ArrayList<>();
                String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
                if (!exclusions.contains(signedInUserId)) {
                    exclusions.add(signedInUserId);
                }
                if (!exclusions.contains(from)) {
                    exclusions.add(from);
                }
                peopleAndGroupsQuery.whereNotContainedIn(AppConstants.REAL_OBJECT_ID, exclusions);
                peopleAndGroupsQuery.whereContains(AppConstants.APP_USER_DISPLAY_NAME, searchString.toLowerCase());
                peopleAndGroupsQuery.setLimit(100);
                if (skip != 0) {
                    peopleAndGroupsQuery.setSkip(skip);
                }
                peopleAndGroupsQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (objects != null && !objects.isEmpty()) {
                                if (skip == 0) {
                                    people.clear();
                                }
                                sortConversations();
                                loadAdapter(objects);
                            }
                        }
                        peopleAndGroupsQuery.cancel();
                    }
                });
            }
        }
    }

    private void loadAdapter(List<ParseObject> users) {
        if (!users.isEmpty()) {
            for (ParseObject parseUser : users) {
                ConversationItem conversationItem = new ConversationItem(parseUser);
                if (!people.contains(conversationItem)) {
                    people.add(conversationItem);
                }
            }
            sortConversations();
        }
        UiUtils.showView(progressWheel, false);
        selectPeopleAdapter.notifyDataSetChanged();
    }

    private void sortConversations() {
        Collections.sort(people);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String s = (String) o;
                    if (s.equals(AppConstants.REFRESH_SELECTED_PEOPLE_TO_FORWARD_MESSAGE)) {
                        if (!AppConstants.selectedPeople.isEmpty()) {
                            doneFab.show();
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle("Forward Message to: (" + AppConstants.selectedPeople.size() + ")");
                            }
                        } else {
                            doneFab.hide();
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle("Forward Message to:");
                            }
                        }
                    }
                }
            }
        });
    }

}
