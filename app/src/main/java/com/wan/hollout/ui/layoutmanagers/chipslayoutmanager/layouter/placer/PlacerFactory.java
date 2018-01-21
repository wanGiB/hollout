package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.placer;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.ChipsLayoutManager;

public class PlacerFactory {

    private ChipsLayoutManager lm;

    public PlacerFactory(ChipsLayoutManager lm) {
        this.lm = lm;
    }

    public IPlacerFactory createRealPlacerFactory() {
        return new RealPlacerFactory(lm);
    }

    public IPlacerFactory createDisappearingPlacerFactory() {
        return new DisappearingPlacerFactory(lm);
    }

}
