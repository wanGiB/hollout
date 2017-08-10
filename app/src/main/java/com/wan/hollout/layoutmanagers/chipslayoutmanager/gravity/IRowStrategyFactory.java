package com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.layoutmanagers.chipslayoutmanager.RowStrategy;

public interface IRowStrategyFactory {
    IRowStrategy createRowStrategy(@RowStrategy int rowStrategy);
}
