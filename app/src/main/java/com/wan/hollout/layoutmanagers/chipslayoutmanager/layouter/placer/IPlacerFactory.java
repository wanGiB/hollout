package com.wan.hollout.layoutmanagers.chipslayoutmanager.layouter.placer;


public interface IPlacerFactory {
    IPlacer getAtStartPlacer();
    IPlacer getAtEndPlacer();
}
