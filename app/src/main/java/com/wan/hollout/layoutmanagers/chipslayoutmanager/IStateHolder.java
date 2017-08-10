package com.wan.hollout.layoutmanagers.chipslayoutmanager;

interface IStateHolder {
    boolean isLayoutRTL();

    @Orientation
    int layoutOrientation();

}
