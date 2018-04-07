package com.wan.hollout.listeners;

import android.support.v4.widget.NestedScrollView;
import android.view.View;

import com.wan.hollout.utils.HolloutLogger;

/**
 * @author Wan Clem
 */

public abstract class NestedViewHideShowScrollListener implements NestedScrollView.OnScrollChangeListener {

    private String TAG = "NestedViewHideShowScrollListener";

    public View view;

    public NestedViewHideShowScrollListener(View view) {
        this.view = view;
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY > oldScrollY && view.getVisibility()==View.VISIBLE) {
            HolloutLogger.i(TAG, "Scroll DOWN");
            onHide();
        }
        if (scrollY < oldScrollY && view.getVisibility()==View.GONE) {
            HolloutLogger.i(TAG, "Scroll UP");
            onShow();
        }

        if (scrollY == 0) {
            HolloutLogger.i(TAG, "TOP SCROLL");
        }

        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
            HolloutLogger.i(TAG, "BOTTOM SCROLL");
        }
    }


    public abstract void onHide();

    public abstract void onShow();

}
