package com.wan.hollout.ui.utils;

import com.wan.hollout.models.Feed;

import java.util.Arrays;
import java.util.List;

/**
 * @author Wan Clem
 */

public class Constants {

    public static int HORIZONTAL_SPACING = DisplayUtil.dpToPx(4);

    public static int VERTICAL_SPACING = DisplayUtil.dpToPx(8);

    public static int HEIGHT_VIEW_REACTION = DisplayUtil.dpToPx(260);

    public static final int MAX_ALPHA = 255;

    public static final List<Feed> feeds = Arrays.asList(new Feed(), new Feed());
}
