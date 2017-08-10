package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.ChipsLayoutManager;
import com.wan.hollout.ui.adapters.PeopleToMeetAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PeopleILikeToMeetActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.people_to_meet_recycler_view)
    RecyclerView peopleToMeetRecyclerView

    @BindView(R.id.selected_people_recycler_view)
    RecyclerView selectedPeopleRecyclerView;

    @BindView(R.id.close_activity)
    ImageView closeActivity;

    @BindView(R.id.done_with_selection)
    ImageView doneWithSelection;

    @BindView(R.id.searchTextView)
    EditText searchTextView;

    @BindView(R.id.action_empty_btn)
    ImageView clearSearchBox;

    private PeopleToMeetAdapter peopleToMeetAdapter, selectedPeopleAdapter;

    private ChipsLayoutManager newPeopleToMeetLayoutManager;
    private LinearLayoutManager selectedPeopleToMeetLayoutManager;

    private List<ParseObject> peopleToMeet = new ArrayList<>();
    private List<ParseObject> alreadySelectedPeople = new ArrayList<>();
    private View peopleToMeetFooter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_people_i_like_to_meet_with);
        ButterKnife.bind(this);
        initEventHandlers();
        initFooters();
    }

    @SuppressLint("InflateParams")
    private void initFooters() {
        peopleToMeetFooter = getLayoutInflater().inflate(R.layout.loading_footer, null);
    }

    private void initNewPeopleToMeetLayoutManager() {
        newPeopleToMeetLayoutManager = ChipsLayoutManager.newBuilder(this)
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
        peopleToMeetRecyclerView.setLayoutManager(newPeopleToMeetLayoutManager);
    }

    private void setupPeopleToMeetAdapter() {
        peopleToMeetAdapter = new PeopleToMeetAdapter(this, peopleToMeet);
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
                Intent callerIntent = new Intent();
                setResult(RESULT_OK, callerIntent);
                finish();
                break;
            case R.id.action_empty_btn:
                searchTextView.setText("");
                break;
        }
    }

}
