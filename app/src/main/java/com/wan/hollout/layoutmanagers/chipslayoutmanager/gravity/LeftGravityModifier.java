package com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity;

import android.graphics.Rect;

class LeftGravityModifier implements IGravityModifier {

    @Override
    public Rect modifyChildRect(int minStart, int maxEnd, Rect childRect) {
        childRect = new Rect(childRect);

        if (childRect.left > minStart) {
            childRect.right -= (childRect.left - minStart);
            childRect.left = minStart;
        }

        return childRect;
    }
}
