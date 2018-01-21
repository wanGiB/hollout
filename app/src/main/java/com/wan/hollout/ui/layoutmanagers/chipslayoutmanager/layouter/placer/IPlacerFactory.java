package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.placer;


public interface IPlacerFactory {
    IPlacer getAtStartPlacer();
    IPlacer getAtEndPlacer();
}
