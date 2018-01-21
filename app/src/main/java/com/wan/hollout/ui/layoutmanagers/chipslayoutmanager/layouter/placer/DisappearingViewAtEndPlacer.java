package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.placer;

import android.support.v7.widget.RecyclerView;
import android.view.View;

class DisappearingViewAtEndPlacer extends AbstractPlacer {

    DisappearingViewAtEndPlacer(RecyclerView.LayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void addView(View view) {
        getLayoutManager().addDisappearingView(view);
    }
}
