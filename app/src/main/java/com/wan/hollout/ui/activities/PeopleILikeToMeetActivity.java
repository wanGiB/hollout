package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.ViewFlipper;

import com.wan.hollout.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PeopleILikeToMeetActivity extends AppCompatActivity {

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.people_to_meet_recycler_view)
    RecyclerView interestsRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_people_i_like_to_meet_with);
        ButterKnife.bind(this);
    }

}
