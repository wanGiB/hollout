package com.wan.hollout.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * A custom linear linear layout manager to avoid subtle bugs in the recycler view design that causes fatal errors
 ****/
@SuppressWarnings("unused")
public class SafeLayoutManager extends LinearLayoutManager {

    public SafeLayoutManager(Context context) {
        super(context);
    }

    public SafeLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SafeLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /***
     * Override this and return false to avoid recycler view throwing inconsistency and invalid position detection
     **/
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}
