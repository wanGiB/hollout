package com.wan.hollout.ui.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * @author Wan Clem
 */

public class DisplayUtil {

    public static int dpToPx(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
