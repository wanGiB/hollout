package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.breaker;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

public class LTRBackwardColumnBreaker implements ILayoutRowBreaker {

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return al.getViewBottom() - al.getCurrentViewHeight() < al.getCanvasTopBorder()
                && al.getViewBottom() < al.getCanvasBottomBorder();
    }

}
