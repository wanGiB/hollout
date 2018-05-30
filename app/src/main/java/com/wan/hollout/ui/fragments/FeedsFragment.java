package com.wan.hollout.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.rendering.StickyRecyclerHeadersDecoration;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.adapters.FeedsAdapter;
import com.wan.hollout.ui.widgets.ChatRequestsHeaderView;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.PhotoLikesHeaderView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings("ConstantConditions")
public class FeedsFragment extends BaseFragment {

    @BindView(R.id.nothing_to_load)
    HolloutTextView nothingToLoadView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.feeds_recycler_view)
    RecyclerView feedsRecyclerView;

    @BindView(R.id.chat_requests_view)
    ChatRequestsHeaderView chatRequestsHeaderView;

    @BindView(R.id.photo_likes_view)
    PhotoLikesHeaderView photoLikesHeaderView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    private FeedsAdapter feedsAdapter;
    private List<ParseObject> feeds = new ArrayList<>();

    private ParseObject signedInUserObject;
    private View footerView;

    private Comparator<ParseObject> feedsSorter = new Comparator<ParseObject>() {
        @Override
        public int compare(ParseObject parseObjectOne, ParseObject parseObjectTwo) {
            if (parseObjectOne != null && parseObjectTwo != null) {
                Date objectOneDate = parseObjectOne.getCreatedAt();
                Date objectTwoDate = parseObjectTwo.getCreatedAt();
                if (objectOneDate != null && objectTwoDate != null) {
                    return objectTwoDate.compareTo(objectOneDate);
                }
            }
            return 0;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_feeds, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        feedsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void checkForNewChatRequests() {
        fetchChatRequests(false);
    }

    private void fetchChatRequests(final boolean canLoadFeeds) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            chatRequestsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects != null && !objects.isEmpty()) {
                        List<ParseObject> firstThreeObjects = HolloutUtils.safeSubList(objects, 0, 3);
                        UiUtils.showView(chatRequestsHeaderView, true);
                        chatRequestsHeaderView.setChatRequests(getActivity(), firstThreeObjects, objects.size());
                        showFeedsAvailableIndicator();
                    } else {
                        if (e != null) {
                            UiUtils.showView(chatRequestsHeaderView, false);
                        }
                    }
                    chatRequestsQuery.cancel();
                    if (canLoadFeeds) {
                        fetchFeeds(0);
                    }
                    fetchPhotoLikes();
                }
            });
        }
        chatRequestsHeaderView.attachEventHandlers(getActivity());
    }

    private void showFeedsAvailableIndicator() {
        //Show activity available dot
        if (MainActivity.tabLayout != null) {
            TabLayout.Tab tab = MainActivity.tabLayout.getTabAt(2);
            if (tab != null) {
                View viewAtTab = tab.getCustomView();
                if (viewAtTab != null && MainActivity.viewPager != null &&
                        MainActivity.viewPager.getCurrentItem() != 2) {
                    UiUtils.showView(viewAtTab.findViewById(R.id.activity_indicator), true);
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        footerView = layoutInflater.inflate(R.layout.loading_footer, null);
        signedInUserObject = AuthUtil.getCurrentUser();
        UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchChatRequests(true);
            }
        });
        setupFeedsAdapter();
        fetchMyPhotoLikesFromFirebase();
        fetchChatRequests(true);
    }

    private void setupFeedsAdapter() {
        feedsAdapter = new FeedsAdapter(getActivity(), feeds);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(feedsAdapter);
        LinearLayoutManager staggeredGridLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        feedsRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        feedsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        feedsRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(feedsAdapter));
        feedsRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        RecyclerViewUtils.setFooterView(feedsRecyclerView, footerView);
        UiUtils.showView(footerView, false);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1) != null) {
                    if ((scrollY >= (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        if (!feeds.isEmpty()) {
                            if (feeds.size() >= 30) {
                                UiUtils.showView(footerView, true);
                            }
                            fetchFeeds(feeds.size());
                        }
                    }
                }
            }
        });
    }

    public void fetchMyPhotoLikesFromFirebase() {
        if (signedInUserObject != null) {
            final String signedInUserId = signedInUserObject.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(signedInUserId)) {
                FirebaseUtils.getPhotoLikesReference().child(signedInUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.exists()) {
                                    long dataSnapShotCount = dataSnapshot.getChildrenCount();
                                    if (dataSnapShotCount != 0) {
                                        GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new
                                                GenericTypeIndicator<HashMap<String, Object>>() {};
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            HashMap<String, Object> photoLike = snapshot.getValue(genericTypeIndicator);
                                            if (photoLike != null) {
                                                Boolean photoPreviewed = (Boolean) photoLike.get(AppConstants.PREVIEWED);
                                                if (photoPreviewed == null) {
                                                    markAsPreviewed(snapshot, signedInUserId);
                                                } else {
                                                    if (!photoPreviewed) {
                                                        markAsPreviewed(snapshot, signedInUserId);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

            }

        }

    }

    private void markAsPreviewed(DataSnapshot snapshot, String signedInUserId) {
        HashMap<String, Object> updatableProps = new HashMap<>();
        updatableProps.put(AppConstants.PREVIEWED, true);
        FirebaseUtils.getPhotoLikesReference()
                .child(signedInUserId)
                .child(snapshot.getKey())
                .updateChildren(updatableProps);
    }

    private void fetchPhotoLikes() {
        final ParseQuery<ParseObject> photoLikesQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
        photoLikesQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUserObject.getString(AppConstants.REAL_OBJECT_ID));
        photoLikesQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_PHOTO_LIKE);
        photoLikesQuery.whereEqualTo(AppConstants.SEEN_BY_OWNER, false);
        photoLikesQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && !objects.isEmpty()) {
                    List<String> likersIds = new ArrayList<>();
                    List<ParseObject> firstThreeUniqueLikers = new ArrayList<>();
                    for (ParseObject object : objects) {
                        String objectId = object.getString(AppConstants.REAL_OBJECT_ID);
                        if (!likersIds.contains(objectId)) {
                            likersIds.add(objectId);
                            if (firstThreeUniqueLikers.size() < 3) {
                                firstThreeUniqueLikers.add(object);
                            }
                        }
                    }
                    UiUtils.showView(photoLikesHeaderView, true);
                    photoLikesHeaderView.setPhotoRequests(getActivity(), firstThreeUniqueLikers, likersIds.size());
                    showFeedsAvailableIndicator();
                } else {
                    if (e != null) {
                        UiUtils.showView(photoLikesHeaderView, false);
                    }
                }
                photoLikesQuery.cancel();
                photoLikesHeaderView.attachEventHandlers(getActivity());
            }
        });
    }

    private void fetchFeeds(final int skip) {
        List<String> signedInUserChats = signedInUserObject.getList(AppConstants.APP_USER_CHATS);
        ParseQuery<ParseObject> userFeedQuery = null;
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            String signedInUserId = signedInUserObject.getString(AppConstants.REAL_OBJECT_ID);
            if (signedInUserChats.contains(null)) {
                signedInUserChats.remove(null);
            }
            if (!signedInUserChats.contains(signedInUserId)) {
                signedInUserChats.add(signedInUserId);
            }
            userFeedQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            List<String> requiredFeedTypes = new ArrayList<>();
            requiredFeedTypes.add(AppConstants.USER_STORIES);
            requiredFeedTypes.add(AppConstants.WORKOUT_REQUESTS);
            userFeedQuery.whereContainedIn(AppConstants.FEED_TYPE, requiredFeedTypes);
            userFeedQuery.whereContainedIn(AppConstants.FEED_CREATOR_ID, signedInUserChats);
            userFeedQuery.include(AppConstants.STORY_LIST);
        }
        if (userFeedQuery != null) {
            if (!feeds.isEmpty()) {
                userFeedQuery.setSkip(skip);
            }
            userFeedQuery.include(AppConstants.FEED_CREATOR);
            userFeedQuery.setLimit(30);
            userFeedQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    List<ParseObject> unseenFeeds = new ArrayList<>();
                    if (e == null && objects != null && !objects.isEmpty()) {
                        if (skip == 0) {
                            feeds.clear();
                            feedsAdapter.notifyDataSetChanged();
                        }
                        for (ParseObject feedObject : objects) {
                            String feedType = feedObject.getString(AppConstants.FEED_TYPE);
                            checkAndDeleteExpiredStories(feedObject, feedType);
                            ParseObject feedCreator = feedObject.getParseObject(AppConstants.FEED_CREATOR);
                            if (feedCreator != null) {
                                List<String> originatorUserChats = feedCreator.getList(AppConstants.APP_USER_CHATS);
                                if (originatorUserChats != null && !originatorUserChats.isEmpty()) {
                                    if (originatorUserChats.contains(signedInUserObject.getString(AppConstants.REAL_OBJECT_ID))) {
                                        feeds.add(feedObject);
                                    }
                                }
                                boolean seen = feedObject.getBoolean(AppConstants.FEED_SEEN);
                                if (!seen) {
                                    unseenFeeds.add(feedObject);
                                }
                            }
                        }
                        Collections.sort(feeds, feedsSorter);
                        feedsAdapter.notifyDataSetChanged();
                    }
                    UiUtils.showView(nothingToLoadView, feeds.isEmpty());
                    UiUtils.showView(progressBar, false);
                    swipeRefreshLayout.setRefreshing(false);
                    UiUtils.showView(footerView, false);

                    if (!unseenFeeds.isEmpty()) {
                        showFeedsAvailableIndicator();
                        unseenFeeds.clear();
                    }
                }
            });
        }
    }

    private void checkAndDeleteExpiredStories(final ParseObject feedObject, String feedType) {
        if (feedType.equals(AppConstants.USER_STORIES)) {
            final List<ParseObject> storyList = feedObject.getList(AppConstants.STORY_LIST);
            if (storyList != null && !storyList.isEmpty()) {
                for (final ParseObject storyObject : storyList) {
                    long statusExpirationTime = storyObject.getLong(AppConstants.STATUS_EXPIRATION);
                    if (System.currentTimeMillis() >= statusExpirationTime) {
                        //This status has expired. Prepare for deletion
                        storyObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (storyList.contains(storyObject)) {
                                    storyList.remove(storyObject);
                                }
                                feedObject.put(AppConstants.STORY_LIST, storyList);
                                feedObject.saveInBackground();
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String s = (String) o;
                    switch (s) {
                        case AppConstants.CHECK_FOR_NEW_CHAT_REQUESTS:
                            checkForNewChatRequests();
                            break;
                        case AppConstants.DISABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(false);
                            break;
                        case AppConstants.ENABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(true);
                            break;
                        case AppConstants.REFRESH_FEEDS:
                            swipeRefreshLayout.setRefreshing(true);
                            fetchFeeds(0);
                            EventBus.getDefault().removeStickyEvent(AppConstants.REFRESH_FEEDS);
                            break;
                    }
                }
            }
        });
    }

}
