package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.util;

import android.view.View;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.IStateFactory;


public class StateHelper {
    public static boolean isInfinite(IStateFactory stateFactory) {
        return stateFactory.getSizeMode() == View.MeasureSpec.UNSPECIFIED
                && stateFactory.getEnd() == 0;
    }
}
