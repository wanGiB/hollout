package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.criteria;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

public class EmtpyCriteria implements IFinishingCriteria {
    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return true;
    }

}
