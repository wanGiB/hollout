package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;
import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.Item;

import java.util.List;

class EmptyRowStrategy implements IRowStrategy {
    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row) {
        //do nothing
    }
}
