package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.anchor.AnchorViewState;

public interface IScrollingController {

    RecyclerView.SmoothScroller createSmoothScroller(@NonNull Context context, int position, int timeMs, AnchorViewState anchor);

    boolean canScrollVertically();

    boolean canScrollHorizontally();

    /**
     * calculate offset of views while scrolling, layout items on new places
     */
    int scrollVerticallyBy(int d, RecyclerView.Recycler recycler, RecyclerView.State state);

    int scrollHorizontallyBy(int d, RecyclerView.Recycler recycler, RecyclerView.State state);

    /** changes may cause gaps on the UI, try to fix them */
    boolean normalizeGaps(RecyclerView.Recycler recycler, RecyclerView.State state);

    int computeVerticalScrollOffset(RecyclerView.State state);

    int computeVerticalScrollExtent(RecyclerView.State state);

    int computeVerticalScrollRange(RecyclerView.State state);

    int computeHorizontalScrollOffset(RecyclerView.State state);

    int computeHorizontalScrollExtent(RecyclerView.State state);

    int computeHorizontalScrollRange(RecyclerView.State state);
}
