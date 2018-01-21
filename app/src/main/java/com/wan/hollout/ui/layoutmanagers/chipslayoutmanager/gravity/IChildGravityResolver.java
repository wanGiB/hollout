package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.SpanLayoutChildGravity;

/** class which determines child gravity inside row from child position */
public interface IChildGravityResolver {
    @SpanLayoutChildGravity
    int getItemGravity(int position);
}
