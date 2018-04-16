package com.wan.hollout.bean;

/**
 * @author Wan Clem
 */

public class PhotoFilter {

    private String filterName;
    private FilterType filterType;

    public PhotoFilter(String filterName, FilterType filterType) {
        this.filterName = filterName;
        this.filterType = filterType;
    }

    public enum FilterType {
        BLACK_FILTER,
        FLEA, GAUSSIAN_BLUR, HUE,
        MEAN_REMOVAL, REFLECTION,
        SATURATION, SHADING_EFFECT,
        SNOW_EFFECT, BOOST, CONTRAST, SEPIA,
        SHADOW_EFFECT, COLOR_DEPTH,
        BRIGHTNESS, COLOR_FILTER, GAMMA,
        GRAY_SCALE, HIGHLIGHT, INVERT,
        EMBOSS, ENGRAVE, FLIP,
        REPLACE_COLOR, ROTATE,
        ROUND_CORNER, SHARPEN, SMOOTH,
        TINT, WATER_MARK
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public String getFilterName() {
        return filterName;
    }

}
