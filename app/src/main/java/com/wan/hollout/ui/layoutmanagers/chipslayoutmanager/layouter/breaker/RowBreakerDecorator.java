package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.breaker;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.AbstractLayouter;

class RowBreakerDecorator implements ILayoutRowBreaker {

    private ILayoutRowBreaker decorate;

    RowBreakerDecorator(ILayoutRowBreaker decorate) {
        this.decorate = decorate;
    }

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return decorate.isRowBroke(al);
    }
}
