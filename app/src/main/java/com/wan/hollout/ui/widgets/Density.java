package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * @author Wan Clem
 */
class Density {
    public static int dp2px(Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }
}
