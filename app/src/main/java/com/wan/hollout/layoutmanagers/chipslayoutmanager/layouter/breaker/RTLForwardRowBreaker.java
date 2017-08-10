package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.breaker;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

class RTLForwardRowBreaker implements ILayoutRowBreaker {

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return al.getViewRight() < al.getCanvasRightBorder()
                && al.getViewRight() - al.getCurrentViewWidth() < al.getCanvasLeftBorder();

    }
}
