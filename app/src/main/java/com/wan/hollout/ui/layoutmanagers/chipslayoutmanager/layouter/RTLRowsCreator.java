package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.anchor.AnchorViewState;

class RTLRowsCreator implements ILayouterCreator {

    private RecyclerView.LayoutManager layoutManager;

    RTLRowsCreator(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    //---- up layouter below
    @Override
    public Rect createOffsetRectForBackwardLayouter(AnchorViewState anchor) {
        Rect anchorRect = anchor.getAnchorViewRect();

        return new Rect(
                //we shouldn't include anchor view here, so anchorLeft is a rightOffset
                anchorRect == null ? 0 : anchorRect.right,
                anchorRect == null ? 0 : anchorRect.top,
                0,
                anchorRect == null ? 0 : anchorRect.bottom);
    }

    @Override
    public AbstractLayouter.Builder createBackwardBuilder() {
        return RTLUpLayouter.newBuilder();
    }

    //---- down layouter below

    @Override
    public AbstractLayouter.Builder createForwardBuilder() {
        return RTLDownLayouter.newBuilder();
    }

    @Override
    public Rect createOffsetRectForForwardLayouter(AnchorViewState anchor) {
        Rect anchorRect = anchor.getAnchorViewRect();

        return new Rect(
                0,
                anchorRect == null ? anchor.getPosition() == 0 ? layoutManager.getPaddingTop() : 0 : anchorRect.top,
                anchorRect == null ? layoutManager.getPaddingRight() : anchorRect.right,
                anchorRect == null ? anchor.getPosition() == 0 ? layoutManager.getPaddingBottom() : 0 : anchorRect.bottom);
    }
}
