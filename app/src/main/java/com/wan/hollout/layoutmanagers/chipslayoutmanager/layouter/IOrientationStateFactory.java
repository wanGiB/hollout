package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter;

import android.support.v7.widget.RecyclerView;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.breaker.IBreakerFactory;

interface IOrientationStateFactory {ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm);
    IRowStrategyFactory createRowStrategyFactory();
    IBreakerFactory createDefaultBreaker();
}
