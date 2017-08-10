package com.wan.hollout.layoutmanagers.chipslayoutmanager;

import android.support.v7.widget.RecyclerView;

interface IDisappearingViewsManager {
    DisappearingViewsManager.DisappearingViewsContainer getDisappearingViews(RecyclerView.Recycler recycler);

    int calcDisappearingViewsLength(RecyclerView.Recycler recycler);

    int getDeletingItemsOnScreenCount();

    void reset();
}
