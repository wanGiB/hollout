package com.wan.hollout.utils;

import com.wan.hollout.bean.PhotoFilter;

/**
 * @author Wan Clem
 */

public class FiltersLoader {

    private static PhotoFilter[] filters = new PhotoFilter[]{
            new PhotoFilter("Black", PhotoFilter.FilterType.BLACK_FILTER),
            new PhotoFilter("Flea", PhotoFilter.FilterType.FLEA),
            new PhotoFilter("Gaussian Blur", PhotoFilter.FilterType.GAUSSIAN_BLUR),
            new PhotoFilter("Hue", PhotoFilter.FilterType.HUE),
            new PhotoFilter("Mean Removal", PhotoFilter.FilterType.MEAN_REMOVAL),
            new PhotoFilter("Reflection", PhotoFilter.FilterType.REFLECTION),
            new PhotoFilter("Saturation", PhotoFilter.FilterType.SATURATION),
            new PhotoFilter("Shading", PhotoFilter.FilterType.SHADING_EFFECT),
            new PhotoFilter("Snow Effect", PhotoFilter.FilterType.SNOW_EFFECT),
            new PhotoFilter("Boost", PhotoFilter.FilterType.BOOST),
            new PhotoFilter("Contrast", PhotoFilter.FilterType.CONTRAST),
            new PhotoFilter("Sepia", PhotoFilter.FilterType.SEPIA),
            new PhotoFilter("Shadow Effect", PhotoFilter.FilterType.SHADOW_EFFECT),
            new PhotoFilter("Color Depth", PhotoFilter.FilterType.COLOR_DEPTH),
            new PhotoFilter("Brightness", PhotoFilter.FilterType.BRIGHTNESS),
            new PhotoFilter("Color Filter", PhotoFilter.FilterType.COLOR_FILTER),
            new PhotoFilter("Gamma", PhotoFilter.FilterType.GAMMA),
            new PhotoFilter("Grey Scale", PhotoFilter.FilterType.GRAY_SCALE),
            new PhotoFilter("Highlight", PhotoFilter.FilterType.HIGHLIGHT),
            new PhotoFilter("Invert", PhotoFilter.FilterType.INVERT),
            new PhotoFilter("Emboss", PhotoFilter.FilterType.EMBOSS),
            new PhotoFilter("Engrave", PhotoFilter.FilterType.ENGRAVE),
            new PhotoFilter("Flip", PhotoFilter.FilterType.FLIP),
            new PhotoFilter("Replace Color", PhotoFilter.FilterType.REPLACE_COLOR),
            new PhotoFilter("Rotate", PhotoFilter.FilterType.ROTATE),
            new PhotoFilter("Round Corners", PhotoFilter.FilterType.ROUND_CORNER),
            new PhotoFilter("Sharpen", PhotoFilter.FilterType.SHARPEN),
            new PhotoFilter("Smooth", PhotoFilter.FilterType.SMOOTH),
            new PhotoFilter("Tint", PhotoFilter.FilterType.TINT),
            new PhotoFilter("Water Mark", PhotoFilter.FilterType.WATER_MARK)
    };

    public static PhotoFilter[] getFilters() {
        return filters;
    }

}
