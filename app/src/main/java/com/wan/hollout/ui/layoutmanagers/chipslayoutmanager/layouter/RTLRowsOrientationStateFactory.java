package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter;

import android.support.v7.widget.RecyclerView;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity.RTLRowStrategyFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.breaker.RTLRowBreakerFactory;

class RTLRowsOrientationStateFactory implements IOrientationStateFactory {

    @Override
    public ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm) {
        return new RTLRowsCreator(lm);
    }

    @Override
    public IRowStrategyFactory createRowStrategyFactory() {
        return new RTLRowStrategyFactory();
    }

    @Override
    public IBreakerFactory createDefaultBreaker() {
        return new RTLRowBreakerFactory();
    }
}
