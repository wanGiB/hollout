package com.wan.hollout.ui.widgets;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Wan on 10/19/2015.
 *
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        HolloutRoundedImageCorner.TOP_LEFT, HolloutRoundedImageCorner.TOP_RIGHT,
        HolloutRoundedImageCorner.BOTTOM_LEFT, HolloutRoundedImageCorner.BOTTOM_RIGHT
})
public @interface HolloutRoundedImageCorner {
    int TOP_LEFT = 0;
    int TOP_RIGHT = 1;
    int BOTTOM_RIGHT = 2;
    int BOTTOM_LEFT = 3;
}
