package com.wan.hollout.layoutmanagers.chipslayoutmanager.util.testing;

import android.support.v7.widget.RecyclerView;

public class EmptySpy implements ISpy {
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //do nothing
    }
}
