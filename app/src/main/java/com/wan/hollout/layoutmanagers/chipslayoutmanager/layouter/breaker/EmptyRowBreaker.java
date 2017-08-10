package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.breaker;


public class EmptyRowBreaker implements IRowBreaker {
    @Override
    public boolean isItemBreakRow(int position) {
        return false;
    }
}
