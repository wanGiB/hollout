package com.wan.hollout.ui.activities;

import android.os.Bundle;

import com.klinker.android.sliding.SlidingActivity;
import com.wan.hollout.R;

/**
 * @author Wan Clem
 */

public class NewsFeedActivity extends SlidingActivity {

    @Override
    public void init(Bundle savedInstanceState) {
        setTitle("News Feed");
        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );
        setContent(R.layout.activity_news_feed);
    }

}
