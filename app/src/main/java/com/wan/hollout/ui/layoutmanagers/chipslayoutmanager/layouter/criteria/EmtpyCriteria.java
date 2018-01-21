package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.criteria;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

public class EmtpyCriteria implements IFinishingCriteria {
    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return true;
    }

}
