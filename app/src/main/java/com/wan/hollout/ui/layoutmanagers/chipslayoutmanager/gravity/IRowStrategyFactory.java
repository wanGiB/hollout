package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.RowStrategy;

public interface IRowStrategyFactory {
    IRowStrategy createRowStrategy(@RowStrategy int rowStrategy);
}
