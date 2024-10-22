package com.wan.hollout.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.ConnectivityChangedAction;
import com.wan.hollout.eventbuses.SearchPeopleEvent;
import com.wan.hollout.models.NearbyPerson;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.adapters.PeopleAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.SafeLayoutManager;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyPeopleFragment extends BaseFragment {

    @BindView(R.id.people_content_flipper)
    public ViewFlipper peopleContentFlipper;

    @BindView(R.id.swipe_refresh_layout)
    public SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.people_recycler_view)
    public RecyclerView peopleRecyclerView;

    @BindView(R.id.no_hollout_users_text_view)
    public HolloutTextView noHolloutTextView;

    @BindView(R.id.card_meet_people)
    CardView cardMeetPeople;

    @BindView(R.id.meet_people_textview)
    HolloutTextView meetPeopleTextView;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    @SuppressLint("StaticFieldLeak")
    public PeopleAdapter peopleAdapter;

    public List<NearbyPerson> nearbyPeople = new ArrayList<>();

    private ParseObject signedInUser;

    private View footerView;

    public String searchString;

    public NearbyPeopleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSignedInUser();
    }

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = AuthUtil.getCurrentUser();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initSignedInUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby_people, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        peopleRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSignedInUser();
        if (getActivity() != null) {
            initBasicViews();
        }
        fetchPeopleOfCommonInterestFromCache();
    }

    private void fetchPeople() {
        swipeRefreshLayout.setRefreshing(true);
        fetchPeopleOfCommonInterestFromCache();
    }

    private void fetchPeopleOfCommonInterestFromCache() {
        final ParseQuery<ParseObject> localUsersQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        localUsersQuery.fromPin(AppConstants.APP_USERS);
        localUsersQuery.orderByAscending(AppConstants.APP_USER_GEO_POINT);
        localUsersQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && !objects.isEmpty()) {
                    loadAdapter(objects);
                    UiUtils.toggleFlipperState(peopleContentFlipper, 2);
                }
                localUsersQuery.cancel();
                fetchPeopleOfCommonInterestsFromNetwork(0);
            }
        });
    }

    @SuppressLint("InflateParams")
    private void initBasicViews() {
        Activity activity = getActivity();
        if (activity != null) {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            footerView = layoutInflater.inflate(R.layout.loading_footer, null);
            UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
            peopleAdapter = new PeopleAdapter(getActivity(), nearbyPeople);
            HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(peopleAdapter);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    fetchPeopleOfCommonInterestsFromNetwork(0);
                }
            });
            SafeLayoutManager linearLayoutManager = new SafeLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            peopleRecyclerView.setLayoutManager(linearLayoutManager);
            peopleRecyclerView.setItemAnimator(new DefaultItemAnimator());
            peopleRecyclerView.setHasFixedSize(true);
            peopleRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
            RecyclerViewUtils.setFooterView(peopleRecyclerView, footerView);
            UiUtils.showView(footerView, false);
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1) != null) {
                        if ((scrollY >= (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                                scrollY > oldScrollY) {
                            //code to fetch more data for endless scrolling
                            if (!nearbyPeople.isEmpty() && nearbyPeople.size() >= 100) {
                                UiUtils.showView(footerView, true);
                                if (StringUtils.isNotEmpty(searchString)) {
                                    searchPeople(nearbyPeople.size(), searchString);
                                } else {
                                    if (nearbyPeople.size() >= 100) {
                                        fetchPeopleOfCommonInterestsFromNetwork(nearbyPeople.size());
                                    }
                                }
                            }
                        }
                    }
                }
            });

            cardMeetPeople.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.blinkView(view);
                    if (meetPeopleTextView.getText().toString().equals(getString(R.string.review_network))) {
                        Intent dataSourceIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(dataSourceIntent);
                    }
                }
            });
        }
    }

    public void fetchPeopleOfCommonInterestsFromNetwork(final int skip) {
        if (getActivity() != null) {
            if (HolloutUtils.isNetWorkConnected(getActivity())) {
                if (signedInUser != null) {
                    List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
                    String filterStartAgeValue = signedInUser.getString(AppConstants.START_AGE_FILTER_VALUE);
                    String filterEndAgeValue = signedInUser.getString(AppConstants.END_AGE_FILTER_VALUE);
                    //Init Query here
                    final ParseQuery<ParseObject> peopleQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
                    peopleQuery.whereEqualTo(AppConstants.OBJECT_TYPE, AppConstants.OBJECT_TYPE_INDIVIDUAL);
                    if (filterStartAgeValue != null && filterEndAgeValue != null) {
                        List<String> ageRanges = HolloutUtils.computeAgeRanges(filterStartAgeValue, filterEndAgeValue);
                        peopleQuery.whereContainedIn(AppConstants.APP_USER_AGE, ageRanges);
                    }
                    String genderFilter = signedInUser.getString(AppConstants.GENDER_FILTER);
                    checkGender(peopleQuery, genderFilter);
                    excludeUserChats(signedInUserChats, peopleQuery);
                    ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
                    attachGeoPoint(peopleQuery, signedInUserGeoPoint);
                    peopleQuery.setLimit(100);
                    peopleQuery.orderByAscending(AppConstants.APP_USER_GEO_POINT);
                    if (skip != 0) {
                        peopleQuery.setSkip(skip);
                    }
                    peopleQuery.findInBackground(new FindCallback<ParseObject>() {

                        @Override
                        public void done(final List<ParseObject> users, final ParseException e) {
                            MainActivity.materialSearchView.hideProgressBar();
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            if (e == null) {
                                if (skip == 0) {
                                    clearPeople();
                                }
                                if (users != null) {
                                    loadAdapter(users);
                                }
                            } else {
                                if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                    displayFetchErrorMessage(true);
                                } else {
                                    displayFetchErrorMessage(false);
                                }
                            }
                            if (!nearbyPeople.isEmpty()) {
                                UiUtils.toggleFlipperState(peopleContentFlipper, 2);
                                cacheListOfPeople();
                            } else {
                                displayFetchErrorMessage(false);
                            }
                            UiUtils.showView(footerView, false);
                            peopleQuery.cancel();
                        }
                    });
                }
            } else {
                displayFetchErrorMessage(true);
            }
        }
    }

    private void attachGeoPoint(ParseQuery<ParseObject> peopleQuery, ParseGeoPoint signedInUserGeoPoint) {
        if (signedInUserGeoPoint != null) {
            peopleQuery.whereWithinKilometers(AppConstants.APP_USER_GEO_POINT, signedInUserGeoPoint, 100.0);
        }
    }

    private void checkGender(ParseQuery<ParseObject> peopleQuery, String genderFilter) {
        if (genderFilter != null && !genderFilter.equals(AppConstants.Both)) {
            peopleQuery.whereEqualTo(AppConstants.APP_USER_GENDER, genderFilter);
        }
    }

    private void excludeUserChats(List<String> savedUserChats, ParseQuery<ParseObject> peopleQuery) {
        if (savedUserChats != null) {
            peopleQuery.whereNotContainedIn(AppConstants.REAL_OBJECT_ID, savedUserChats);
        }
    }

    private void cacheListOfPeople() {
        ParseObject.unpinAllInBackground(AppConstants.APP_USERS, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                List<ParseObject> peopleToPin = new ArrayList<>();
                for (NearbyPerson nearbyPerson : nearbyPeople) {
                    peopleToPin.add(nearbyPerson.getPerson());
                }
                ParseObject.pinAllInBackground(AppConstants.APP_USERS, peopleToPin);
            }
        });
    }

    private void loadAdapter(List<ParseObject> users) {
        if (!users.isEmpty()) {
            for (ParseObject parseUser : users) {
                NearbyPerson nearbyPerson = new NearbyPerson(parseUser);
                if (!nearbyPerson.getPerson().getString(AppConstants.REAL_OBJECT_ID)
                        .equals(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                    if (!nearbyPeople.contains(nearbyPerson)) {
                        nearbyPeople.add(nearbyPerson);
                    }
                }
            }
            Collections.sort(nearbyPeople);
            notifyDataSetChanged();
        }
    }

    private void notifyDataSetChanged() {
        peopleAdapter.notifyDataSetChanged();
    }

    private void displayFetchErrorMessage(boolean networkError) {
        if (nearbyPeople.isEmpty() && getActivity() != null && signedInUser != null) {
            UiUtils.toggleFlipperState(peopleContentFlipper, 1);
            if (networkError) {
                noHolloutTextView.setText(getString(R.string.screwed_data_error_message));
                UiUtils.showView(meetPeopleTextView, true);
                meetPeopleTextView.setText(getString(R.string.review_network));
            } else {
                noHolloutTextView.setText(getString(R.string.people_unavailable));
                UiUtils.showView(meetPeopleTextView, false);
            }
        }
    }

    private void searchPeople(final int skip, String searchString) {
        peopleAdapter.setSearchString(searchString);
        final ParseQuery<ParseObject> parseUserParseQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        parseUserParseQuery.whereEqualTo(AppConstants.OBJECT_TYPE, AppConstants.OBJECT_TYPE_INDIVIDUAL);
        parseUserParseQuery.whereContains(AppConstants.SEARCH_CRITERIA, searchString);
        parseUserParseQuery.orderByAscending(AppConstants.APP_USER_GEO_POINT);
        parseUserParseQuery.setLimit(100);
        if (skip != 0) {
            parseUserParseQuery.setSkip(skip);
        }
        parseUserParseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                MainActivity.materialSearchView.hideProgressBar();
                if (e == null) {
                    if (objects != null && !objects.isEmpty()) {
                        if (skip == 0) {
                            clearPeople();
                        }
                        loadAdapter(objects);
                    }
                    UiUtils.toggleFlipperState(peopleContentFlipper, 2);
                }
                parseUserParseQuery.cancel();
            }
        });
        if (signedInUser != null) {
            signedInUser.put(AppConstants.USER_LAST_SEARCH, searchString);
            signedInUser.pinInBackground();
        }
    }

    @Override
    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof ConnectivityChangedAction) {
                    ConnectivityChangedAction connectivityChangedAction = (ConnectivityChangedAction) o;
                    if (connectivityChangedAction.isConnectivityChanged()) {
                        if (nearbyPeople.isEmpty()) {
                            UiUtils.toggleFlipperState(peopleContentFlipper, 0);
                            fetchPeopleOfCommonInterestsFromNetwork(0);
                        }
                    }
                } else if (o instanceof String) {
                    String action = (String) o;
                    switch (action) {
                        case AppConstants.DISABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(false);
                            break;
                        case AppConstants.ENABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(true);
                            break;
                        case AppConstants.REFRESH_PEOPLE:
                            UiUtils.toggleFlipperState(peopleContentFlipper, 0);
                            fetchPeopleOfCommonInterestsFromNetwork(0);
                            break;
                        case AppConstants.SEARCH_VIEW_CLOSED:
                            peopleAdapter.setSearchString(null);
                            searchString = null;
                            clearPeople();
                            fetchPeople();
                            break;
                    }
                } else if (o instanceof SearchPeopleEvent) {
                    SearchPeopleEvent searchPeopleEvent = (SearchPeopleEvent) o;
                    String queryString = searchPeopleEvent.getQueryString();
                    searchString = queryString;
                    searchPeople(0, queryString);
                }
                EventBus.getDefault().removeAllStickyEvents();
            }

        });

    }

    private void clearPeople() {
        if (!nearbyPeople.isEmpty()) {
            nearbyPeople.clear();
            notifyDataSetChanged();
        }
    }

}
