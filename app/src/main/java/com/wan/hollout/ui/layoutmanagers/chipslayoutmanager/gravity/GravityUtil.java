package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

abstract class GravityUtil {

    static int getHorizontalDifference(AbstractLayouter layouter) {
        return layouter.getCanvasRightBorder() - layouter.getCanvasLeftBorder() - layouter.getRowLength();
    }

    static int getVerticalDifference(AbstractLayouter layouter) {
        return layouter.getCanvasBottomBorder() - layouter.getCanvasTopBorder() - layouter.getRowLength();
    }

}
