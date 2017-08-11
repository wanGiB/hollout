package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.EndlessRecyclerViewScrollListener;
import com.wan.hollout.eventbuses.SelectedPerson;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.ChipsLayoutManager;
import com.wan.hollout.ui.adapters.PeopleToMeetAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
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

public class MeetPeopleActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.people_to_meet_recycler_view)
    RecyclerView potentialPeopleToMeetRecyclerView;

    @BindView(R.id.selected_people_recycler_view)
    RecyclerView selectedPeopleToMeetRecyclerView;

    @BindView(R.id.close_activity)
    ImageView closeActivity;

    @BindView(R.id.done_with_selection)
    ImageView doneWithSelection;

    @BindView(R.id.searchTextView)
    EditText searchTextView;

    @BindView(R.id.action_empty_btn)
    ImageView clearSearchBox;

    @BindView(R.id.error_message)
    HolloutTextView errorMessageView;

    @BindView(R.id.retry)
    HolloutTextView retry;

    @BindView(R.id.no_result_found_view)
    HolloutTextView noResultFoundView;

    private PeopleToMeetAdapter potentialPeopleToMeetAdapter, selectedPeopleToMeetAdapter;

    private ChipsLayoutManager potentialPeopleToMeetLayoutManager;

    private List<ParseObject> potentialPeopleToMeet = new ArrayList<>();
    private List<ParseObject> selectedPeopleToMeet = new ArrayList<>();
    private View potentialPeopleToMeetFooterView;

    private ParseUser signedInUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_people_i_like_to_meet_with);
        ButterKnife.bind(this);
        signedInUser = ParseUser.getCurrentUser();
        initEventHandlers();
        initFooters();
        setupPotentialPeopleToMeetAdapter();
        setupSelectedPeopleToMeetAdapter();
        fetchPeople(null, 0);

        retry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (StringUtils.isEmpty(searchTextView.getText().toString().trim())) {
                    fetchPeople(null, 0);
                } else {
                    fetchPeople(searchTextView.getText().toString().trim(), 0);
                }
            }

        });

        searchTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                searchTextView.setCursorVisible(true);
            }

        });

        searchTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.isEmpty(charSequence.toString().trim())) {
                    fetchPeople(null, 0);
                } else {
                    searchTextView.setCursorVisible(false);
                    fetchPeople(charSequence.toString().trim(), 0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        checkAndRegEventBus();

    }

    @SuppressLint("InflateParams")
    private void initFooters() {
        potentialPeopleToMeetFooterView = getLayoutInflater().inflate(R.layout.loading_footer, null);
    }

    private void initNewPeopleToMeetLayoutManager() {
        potentialPeopleToMeetLayoutManager = ChipsLayoutManager.newBuilder(this)
                //whether RecyclerView can scroll. TRUE by default
                .setScrollingEnabled(true)
                //set maximum views count in a particular row
                .setMaxViewsInRow(3)
                //set gravity resolver where you can determine gravity for item in position.
                //This method have priority over previous one
                //you are able to break row due to your conditions. Row breaker should return true for that views
                //a layoutOrientation of layout manager, could be VERTICAL OR HORIZONTAL. HORIZONTAL by default
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                // row strategy for views in completed row, could be STRATEGY_DEFAULT, STRATEGY_FILL_VIEW,
                //STRATEGY_FILL_SPACE or STRATEGY_CENTER
                .setRowStrategy(ChipsLayoutManager.STRATEGY_FILL_SPACE)
                // whether strategy is applied to last row. FALSE by default
                .withLastRow(true)
                .build();
        potentialPeopleToMeetRecyclerView.setLayoutManager(potentialPeopleToMeetLayoutManager);
    }

    private void setupPotentialPeopleToMeetAdapter() {
        potentialPeopleToMeetAdapter = new PeopleToMeetAdapter(this, potentialPeopleToMeet, AppConstants.PEOPLE_TO_MEET_HOST_TYPE_POTENTIAL);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(potentialPeopleToMeetAdapter);
        initNewPeopleToMeetLayoutManager();
        potentialPeopleToMeetRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        RecyclerViewUtils.setFooterView(potentialPeopleToMeetRecyclerView, potentialPeopleToMeetFooterView);
        UiUtils.showView(potentialPeopleToMeetFooterView, false);
        potentialPeopleToMeetRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(potentialPeopleToMeetLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!potentialPeopleToMeet.isEmpty()) {
                    UiUtils.showView(potentialPeopleToMeetFooterView, true);
                    if (StringUtils.isEmpty(searchTextView.getText().toString().trim())) {
                        fetchPeople(null, potentialPeopleToMeet.size() - 1);
                    } else {
                        fetchPeople(searchTextView.getText().toString().trim(), potentialPeopleToMeet.size() - 1);
                    }
                }
            }
        });
    }

    private void fetchPeople(final String searchString, final int skip) {
        ParseQuery<ParseObject> peopleQuery = ParseQuery.getQuery(AppConstants.INTERESTS);
        if (StringUtils.isNotEmpty(searchString)) {
            peopleQuery.whereContains(AppConstants.NAME, searchString.toLowerCase());
        }
        peopleQuery.setLimit(30);
        if (skip != 0) {
            peopleQuery.setSkip(skip);
        }
        peopleQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (StringUtils.isNotEmpty(searchString)) {
                        potentialPeopleToMeetAdapter.setSearchedString(searchString);
                    }
                    if (objects != null) {
                        if (!objects.isEmpty()) {
                            UiUtils.toggleFlipperState(contentFlipper, 2);
                            loadPeople(objects, skip, searchString);
                        } else {
                            tryShowNoResultFound(searchString);
                        }
                    } else {
                        tryShowNoResultFound(searchString);
                    }
                } else {
                    if (searchString == null) {
                        if (potentialPeopleToMeet.isEmpty()) {
                            UiUtils.toggleFlipperState(contentFlipper, 1);
                            String errorMessage = e.getMessage();
                            if (StringUtils.isNotEmpty(errorMessage)) {
                                if (!errorMessage.contains("i/o")) {
                                    errorMessageView.setText(errorMessage);
                                } else {
                                    errorMessageView.setText(getString(R.string.screwed_data_error_message));
                                }
                            } else {
                                errorMessageView.setText(getString(R.string.screwed_data_error_message));
                            }
                        }
                    } else {
                        tryShowNoResultFound(searchString);
                    }
                }
                UiUtils.showView(potentialPeopleToMeetFooterView, false);
            }
        });
    }

    private void tryShowNoResultFound(String searchString) {
        if (searchString != null) {
            UiUtils.toggleFlipperState(contentFlipper, 2);
            UiUtils.showView(potentialPeopleToMeetRecyclerView, false);
            UiUtils.showView(noResultFoundView, true);
        }
    }

    private void loadPeople(List<ParseObject> objects, int skip, String searchString) {
        List<String> signedInUserInterests = signedInUser.getList(AppConstants.INTERESTS);
        ParseObject.pinAllInBackground(objects);
        if (skip == 0) {
            clearData();
        }
        for (ParseObject parseObject : objects) {
            String name = parseObject.getString(AppConstants.NAME);
            if (signedInUserInterests != null) {
                if (signedInUserInterests.contains(name)) {
                    if (!selectedPeopleToMeet.contains(parseObject) && searchString == null) {
                        parseObject.put(AppConstants.SELECTED, true);
                        selectedPeopleToMeet.add(parseObject);
                        selectedPeopleToMeetAdapter.notifyItemInserted(selectedPeopleToMeet.size() - 1);
                    }
                } else {
                    if (!potentialPeopleToMeet.contains(parseObject)) {
                        potentialPeopleToMeet.add(parseObject);
                        potentialPeopleToMeetAdapter.notifyItemInserted(potentialPeopleToMeet.size() - 1);
                    }
                }
            } else {
                if (!potentialPeopleToMeet.contains(parseObject)) {
                    potentialPeopleToMeet.add(parseObject);
                    potentialPeopleToMeetAdapter.notifyItemInserted(potentialPeopleToMeet.size() - 1);
                }
            }
        }
    }

    private void clearData() {
        selectedPeopleToMeet.clear();
        potentialPeopleToMeet.clear();
        selectedPeopleToMeetAdapter.notifyDataSetChanged();
        potentialPeopleToMeetAdapter.notifyDataSetChanged();
    }

    private void setupSelectedPeopleToMeetAdapter() {
        selectedPeopleToMeetAdapter = new PeopleToMeetAdapter(this, selectedPeopleToMeet, AppConstants.PEOPLE_TO_MEET_HOST_TYPE_SELECTED);
        LinearLayoutManager selectedPeopleToMeetLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        selectedPeopleToMeetRecyclerView.setLayoutManager(selectedPeopleToMeetLayoutManager);
        selectedPeopleToMeetRecyclerView.setAdapter(selectedPeopleToMeetAdapter);
    }

    private void initEventHandlers() {
        closeActivity.setOnClickListener(this);
        doneWithSelection.setOnClickListener(this);
        clearSearchBox.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_activity:
                sendBackResultToCaller();
                break;
            case R.id.action_empty_btn:
                searchTextView.setText("");
                break;
            case R.id.done_with_selection:
                UiUtils.showProgressDialog(MeetPeopleActivity.this, "Please wait...");
                signedInUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        UiUtils.dismissProgressDialog();
                        List<String> userInterests = ParseUser.getCurrentUser().getList(AppConstants.INTERESTS);
                        HolloutUtils.updateCurrentParseInstallation(userInterests, null);
                        sendBackResultToCaller();
                    }
                });
                break;
        }
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
    protected void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
    }


    private void sendBackResultToCaller() {
        Intent callerIntent = new Intent();
        setResult(RESULT_OK, callerIntent);
        finish();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof SelectedPerson) {
                    SelectedPerson selectedPerson = (SelectedPerson) o;
                    ParseObject parseObject = selectedPerson.getPersonObject();
                    if (parseObject != null) {
                        if (selectedPerson.isSelected()) {
                            if (!selectedPeopleToMeet.contains(parseObject)) {
                                selectedPeopleToMeet.add(parseObject);
                                selectedPeopleToMeetAdapter.notifyDataSetChanged();
                                selectedPeopleToMeetRecyclerView.smoothScrollToPosition(selectedPeopleToMeet.size() - 1);
                            }
                        } else {
                            if (selectedPeopleToMeet.contains(parseObject)) {
                                int position = selectedPeopleToMeet.indexOf(parseObject);
                                if (position != -1) {
                                    selectedPeopleToMeet.remove(parseObject);
                                    selectedPeopleToMeetAdapter.notifyItemRemoved(position);
                                    selectedPeopleToMeetRecyclerView.smoothScrollToPosition(position);
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
