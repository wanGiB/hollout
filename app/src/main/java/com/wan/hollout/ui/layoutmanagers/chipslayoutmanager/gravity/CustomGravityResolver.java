package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.SpanLayoutChildGravity;

public class CustomGravityResolver implements IChildGravityResolver {

    @SpanLayoutChildGravity
    private int gravity;

    public CustomGravityResolver(int gravity) {
        this.gravity = gravity;
    }

    @Override
    @SpanLayoutChildGravity
    public int getItemGravity(int position) {
        return gravity;
    }
}
