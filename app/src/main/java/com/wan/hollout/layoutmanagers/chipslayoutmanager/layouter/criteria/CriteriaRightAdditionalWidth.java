package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.criteria;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

class CriteriaRightAdditionalWidth extends FinishingCriteriaDecorator {

    private int additionalWidth;

    CriteriaRightAdditionalWidth(IFinishingCriteria finishingCriteria, int additionalWidth) {
        super(finishingCriteria);
        this.additionalWidth = additionalWidth;
    }

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        int rightBorder = abstractLayouter.getCanvasRightBorder();
        return super.isFinishedLayouting(abstractLayouter) &&
                //if additional height filled
                abstractLayouter.getViewLeft() > rightBorder + additionalWidth;
    }

}
