package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter;

import android.support.v7.widget.RecyclerView;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity.IRowStrategyFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.breaker.IBreakerFactory;

interface IOrientationStateFactory {ILayouterCreator createLayouterCreator(RecyclerView.LayoutManager lm);
    IRowStrategyFactory createRowStrategyFactory();
    IBreakerFactory createDefaultBreaker();
}
