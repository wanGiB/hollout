package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.breaker;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

class LTRForwardRowBreaker implements ILayoutRowBreaker {

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return al.getViewLeft() > al.getCanvasLeftBorder()
                && al.getViewLeft() + al.getCurrentViewWidth() > al.getCanvasRightBorder();
    }
}
