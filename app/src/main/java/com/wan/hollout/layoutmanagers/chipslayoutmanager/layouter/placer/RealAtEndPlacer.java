package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.placer;

import android.support.v7.widget.RecyclerView;
import android.view.View;

class RealAtEndPlacer extends AbstractPlacer implements IPlacer {

    RealAtEndPlacer(RecyclerView.LayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void addView(View view) {
        getLayoutManager().addView(view);
    }

}
