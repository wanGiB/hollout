package com.wan.hollout.ui.fragments;

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
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.rendering.StickyRecyclerHeadersDecoration;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.adapters.PhotoLikesAdapter;
import com.wan.hollout.ui.widgets.ChatRequestsHeaderView;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

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
public class ActivitiesFragment extends BaseFragment {

    @BindView(R.id.nothing_to_load)
    HolloutTextView nothingToLoadView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.activities_recycler_view)
    RecyclerView photoLikesRecyclerView;

    @BindView(R.id.chat_requests_view)
    ChatRequestsHeaderView chatRequestsHeaderView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    private PhotoLikesAdapter photoLikesAdapter;
    private List<ParseObject> activities = new ArrayList<>();

    private ParseObject signedInUserObject;

    private Comparator<ParseObject> activitiesSorter = new Comparator<ParseObject>() {
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
        View fragmentView = inflater.inflate(R.layout.fragment_activities, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    private void countChatRequests() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            chatRequestsQuery.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (e == null && count != 0) {
                        fetchChatRequests(count);
                    } else {
                        if (e != null) {
                            UiUtils.showView(chatRequestsHeaderView, false);
                        }
                    }
                    chatRequestsQuery.cancel();
                }
            });
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
                            countChatRequests();
                            break;
                        case AppConstants.DISABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(false);
                            break;
                        case AppConstants.ENABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(true);
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoLikesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void checkAutoInvitationAccepted() {
        countChatRequests();
    }

    private void fetchChatRequests(final int totalCount) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            chatRequestsQuery.setLimit(3);
            chatRequestsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects != null && !objects.isEmpty()) {
                        UiUtils.showView(chatRequestsHeaderView, true);
                        chatRequestsHeaderView.setChatRequests(getActivity(), objects, totalCount);
                        chatRequestsHeaderView.showNearbyHeader(true);
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
                    chatRequestsQuery.cancel();
                }
            });
        }
        chatRequestsHeaderView.attachEventHandlers(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAutoInvitationAccepted();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        signedInUserObject = AuthUtil.getCurrentUser();
        UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                countChatRequests();
                fetchMyPhotoLikesFromParse(0);
            }
        });
        setupPhotoLikesAdapter();
        fetchMyPhotoLikesFromFirebase();
        fetchMyPhotoLikesFromParse(0);
        countChatRequests();
        checkAutoInvitationAccepted();
    }

    private void setupPhotoLikesAdapter() {
        photoLikesAdapter = new PhotoLikesAdapter(getActivity(), activities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        photoLikesRecyclerView.setLayoutManager(linearLayoutManager);
        photoLikesRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        photoLikesRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(photoLikesAdapter));
        photoLikesRecyclerView.setAdapter(photoLikesAdapter);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1) != null) {
                    if ((scrollY >= (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        if (!activities.isEmpty()) {
                            fetchMyPhotoLikesFromParse(activities.size());
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
                                                GenericTypeIndicator<HashMap<String, Object>>() {
                                                };
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

    private void fetchMyPhotoLikesFromParse(final int skip) {
        ParseQuery<ParseObject> photoLikesQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
        photoLikesQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUserObject.getString(AppConstants.REAL_OBJECT_ID));
        photoLikesQuery.include(AppConstants.FEED_CREATOR);
        photoLikesQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_PHOTO_LIKE);
        if (!activities.isEmpty()) {
            photoLikesQuery.setSkip(skip);
        }
        photoLikesQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && !objects.isEmpty()) {
                    if (skip == 0) {
                        activities.clear();
                    }
                    activities.addAll(objects);
                    Collections.sort(activities, activitiesSorter);
                    photoLikesAdapter.notifyDataSetChanged();
                }
                UiUtils.showView(nothingToLoadView, activities.isEmpty());
                UiUtils.showView(progressBar, false);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}
