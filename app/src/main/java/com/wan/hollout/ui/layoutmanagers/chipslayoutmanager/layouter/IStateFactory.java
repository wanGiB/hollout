package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter;

import android.view.View;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.IScrollingController;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.anchor.AnchorViewState;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.anchor.IAnchorFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.criteria.AbstractCriteriaFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.criteria.ICriteriaFactory;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.placer.IPlacerFactory;

public interface IStateFactory {
    @SuppressWarnings("UnnecessaryLocalVariable")
    LayouterFactory createLayouterFactory(ICriteriaFactory criteriaFactory, IPlacerFactory placerFactory);

    AbstractCriteriaFactory createDefaultFinishingCriteriaFactory();

    IAnchorFactory anchorFactory();

    IScrollingController scrollingController();

    ICanvas createCanvas();

    int getSizeMode();

    int getStart();

    int getStart(View view);

    int getStart(AnchorViewState anchor);

    int getStartAfterPadding();

    int getStartViewPosition();

    int getStartViewBound();

    int getEnd();

    int getEnd(View view);

    int getEndAfterPadding();

    int getEnd(AnchorViewState anchor);

    int getEndViewPosition();

    int getEndViewBound();

    int getTotalSpace();
}
