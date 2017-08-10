package com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;
import com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.Item;

import java.util.List;

public interface IRowStrategy {
    void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row);
}
