package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.wan.hollout.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PeopleILikeToMeetActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.people_to_meet_recycler_view)
    RecyclerView interestsRecyclerView;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_people_i_like_to_meet_with);
        ButterKnife.bind(this);
        initEventHandlers();
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
