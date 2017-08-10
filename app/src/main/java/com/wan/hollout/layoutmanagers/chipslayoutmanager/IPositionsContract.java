package com.wan.hollout.layoutmanagers.chipslayoutmanager;

interface IPositionsContract {
    int findFirstVisibleItemPosition();
    int findFirstCompletelyVisibleItemPosition();
    int findLastVisibleItemPosition();
    int findLastCompletelyVisibleItemPosition();
}
