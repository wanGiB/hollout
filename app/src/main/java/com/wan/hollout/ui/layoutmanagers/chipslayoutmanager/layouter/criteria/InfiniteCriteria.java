package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.criteria;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

class InfiniteCriteria implements IFinishingCriteria {

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return false;
    }

}