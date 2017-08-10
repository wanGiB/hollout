package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter;

import android.support.v7.widget.RecyclerView;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity.LTRRowStrategyFactory;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.breaker.LTRRowBreakerFactory;

class LTRRowsOrientationStateFactory implements IOrientationStateFactory {

    @Override
    public ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm) {
        return new LTRRowsCreator(lm);
    }

    @Override
    public IRowStrategyFactory createRowStrategyFactory() {
        return new LTRRowStrategyFactory();
    }

    @Override
    public IBreakerFactory createDefaultBreaker() {
        return new LTRRowBreakerFactory();
    }
}
