package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

    @BindView(R.id.selected_header_text)
    HolloutTextView selectedHeaderTextView;

    private PeopleToMeetAdapter potentialPeopleToMeetAdapter, selectedPeopleToMeetAdapter;

    private ChipsLayoutManager potentialPeopleToMeetLayoutManager;

    private List<ParseObject> potentialPeopleToMeet = new ArrayList<>();
    private List<ParseObject> selectedPeopleToMeet = new ArrayList<>();
    private View potentialPeopleToMeetFooterView;

    private ParseUser signedInUser;

    private final long DELAY = 1000; // in ms

    private Handler handler = new Handler();

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
        offloadUserInterestsIfAvailable();
        fetchPeople(0);

        retry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (StringUtils.isNotEmpty(searchTextView.getText().toString().trim())) {
                    filterPeople(searchTextView.getText().toString().trim(), 0);
                } else {
                    fetchPeople(0);
                }
            }

        });

        searchTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isNotEmpty(searchTextView.getText().toString().trim())) {
                            filterPeople(searchTextView.getText().toString().trim(), 0);
                        } else {
                            fetchPeople(0);
                        }
                    }
                }, DELAY);
            }

        });

        checkAndRegEventBus();

        searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, @NonNull KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!event.isShiftPressed()) {
                        // the user is done typing.
                        filterPeople(searchTextView.getText().toString().trim(), 0);
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }

        });

    }

    private void offloadUserInterestsIfAvailable() {
        if (signedInUser != null) {
            List<String> userInterests = signedInUser.getList(AppConstants.INTERESTS);
            if (userInterests != null) {
                if (!userInterests.isEmpty()) {
                    selectedHeaderTextView.setText(UiUtils.fromHtml("SELECTED (<font color=#E8A723>" + userInterests.size() + "</font>)"));
                    for (String interest : userInterests) {
                        ParseObject interestsObject = new ParseObject(AppConstants.INTERESTS);
                        interestsObject.put(AppConstants.NAME, interest.toLowerCase());
                        interestsObject.put(AppConstants.SELECTED, true);
                        if (!selectedPeopleToMeet.contains(interestsObject)) {
                            selectedPeopleToMeet.add(interestsObject);
                        }
                    }
                }
            }
        }
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
                if (!potentialPeopleToMeet.isEmpty() && StringUtils.isEmpty(searchTextView.getText().toString().trim())) {
                    UiUtils.toggleFlipperState(contentFlipper, 2);
                    UiUtils.showView(potentialPeopleToMeetRecyclerView, true);
                    UiUtils.showView(potentialPeopleToMeetFooterView, true);
                    fetchPeople(potentialPeopleToMeet.size());
                } else if (!potentialPeopleToMeet.isEmpty() && StringUtils.isNotEmpty(searchTextView.getText().toString().trim())) {
                    UiUtils.showView(potentialPeopleToMeetFooterView, true);
                    filterPeople(searchTextView.getText().toString().trim(), potentialPeopleToMeet.size());
                }
            }
        });
    }

    private void fetchPeople(final int skip) {
        potentialPeopleToMeetAdapter.setSearchedString(null);
        ParseQuery<ParseObject> peopleSearch = ParseQuery.getQuery(AppConstants.INTERESTS);
        if (skip != 0) {
            peopleSearch.setSkip(skip);
        }
        peopleSearch.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, final ParseException e) {
                if (objects != null && !objects.isEmpty()) {
                    UiUtils.toggleFlipperState(contentFlipper, 2);
                    loadNewPeopleToMeetAdapter(objects, skip);
                }
                checkListIsEmpty();
                UiUtils.showView(potentialPeopleToMeetFooterView, false);
            }
        });
    }

    private void filterPeople(final String filterString, final int skip) {
        final ParseQuery<ParseObject> peopleSearch = ParseQuery.getQuery(AppConstants.INTERESTS);
        potentialPeopleToMeetAdapter.setSearchedString(filterString);
        peopleSearch.whereContains(AppConstants.NAME, StringUtils.stripEnd(filterString.trim().toLowerCase(), "s"));
        peopleSearch.setLimit(100);
        if (skip != 0) {
            peopleSearch.setSkip(skip);
        }
        peopleSearch.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(final List<ParseObject> objects, final ParseException e) {
                if (objects != null && !objects.isEmpty()) {
                    UiUtils.toggleFlipperState(contentFlipper, 2);
                    loadNewPeopleToMeetAdapter(objects, skip);
                } else {
                    if (skip == 0) {
                        potentialPeopleToMeet.clear();
                        potentialPeopleToMeetAdapter.notifyDataSetChanged();
                    }
                }
                checkListIsEmpty();
                UiUtils.showView(potentialPeopleToMeetFooterView, false);
            }
        });
    }

    private void checkListIsEmpty() {
        UiUtils.toggleFlipperState(contentFlipper, 2);
        UiUtils.showView(potentialPeopleToMeetRecyclerView, !potentialPeopleToMeet.isEmpty());
        UiUtils.showView(noResultFoundView, potentialPeopleToMeet.isEmpty());
    }

    private void loadNewPeopleToMeetAdapter(List<ParseObject> displayableList, int skip) {
        if (skip == 0) {
            potentialPeopleToMeet.clear();
        }
        potentialPeopleToMeet.addAll(displayableList);
        potentialPeopleToMeetAdapter.notifyDataSetChanged();
    }

    private void notifySelectedPeopleToMeetAdapter() {
        try {
            if (selectedPeopleToMeetAdapter.getItemCount() <= 2) {
                selectedPeopleToMeetAdapter.notifyDataSetChanged();
            } else {
                selectedPeopleToMeetAdapter.notifyItemInserted(selectedPeopleToMeet.size() - 1);
                selectedPeopleToMeetAdapter.notifyDataSetChanged();
            }
        } catch (IllegalStateException ignored) {

        }
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
                            parseObject.put(AppConstants.SELECTED, true);
                            if (!selectedPeopleToMeet.contains(parseObject)) {
                                selectedPeopleToMeet.add(parseObject);
                                notifySelectedPeopleToMeetAdapter();
                                selectedPeopleToMeetRecyclerView.smoothScrollToPosition(selectedPeopleToMeet.size() - 1);
                            }
                        } else {
                            if (selectedPeopleToMeet.contains(parseObject)) {
                                int position = selectedPeopleToMeet.indexOf(parseObject);
                                if (position != -1) {
                                    selectedPeopleToMeet.remove(parseObject);
                                    selectedPeopleToMeetAdapter.notifyItemRemoved(position);
                                    selectedPeopleToMeetAdapter.notifyDataSetChanged();
                                    selectedPeopleToMeetRecyclerView.smoothScrollToPosition(position);
                                }
                            }
                        }
                        selectedHeaderTextView.setText(UiUtils.fromHtml("SELECTED (<font color=#E8A723>" + selectedPeopleToMeet.size() + "</font>)"));
                    }
                }
            }
        });
    }
}
