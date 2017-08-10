package com.wan.hollout.layoutmanagers.chipslayoutmanager.util;

import android.view.View;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.IStateFactory;


public class StateHelper {
    public static boolean isInfinite(IStateFactory stateFactory) {
        return stateFactory.getSizeMode() == View.MeasureSpec.UNSPECIFIED
                && stateFactory.getEnd() == 0;
    }
}
