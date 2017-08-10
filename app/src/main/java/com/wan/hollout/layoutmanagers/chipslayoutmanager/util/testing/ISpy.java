package com.wan.hollout.layoutmanagers.chipslayoutmanager.util.testing;

import android.support.v7.widget.RecyclerView;

public interface ISpy {
    void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state);
}
