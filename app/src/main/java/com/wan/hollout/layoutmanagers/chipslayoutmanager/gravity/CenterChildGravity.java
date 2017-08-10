package com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity;

import android.view.Gravity;

import com.wan.hollout.layoutmanagers.chipslayoutmanager.SpanLayoutChildGravity;

public class CenterChildGravity implements IChildGravityResolver {
    @Override
    @SpanLayoutChildGravity
    public int getItemGravity(int position) {
        return Gravity.CENTER_VERTICAL;
    }
}
