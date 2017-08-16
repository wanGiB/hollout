package com.wan.hollout.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wan Clem
 */
public class FontUtils {

    private static String FONT_REGULAR = "AvenirLTStd-Book.otf";
    private static String FONT_BOLD = "AvenirLTStd-Heavy.otf";
    private static String FONT_LIGHT = "AvenirLTStd-Light.otf";
    private static String FONT_MEDIUM = "AvenirLTStd-Medium.otf";
    private static String DYSPEPSIA = "colophon.ttf";
    private static String FONT_AWESOME = "fontawesome-webfont.ttf";

    private static Map<String, Typeface> sCachedFonts = new HashMap<>();

    private static Typeface getTypeface(Context context, String assetPath) {
        if (!sCachedFonts.containsKey(assetPath)) {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), assetPath);
            sCachedFonts.put(assetPath, tf);
        }
        return sCachedFonts.get(assetPath);
    }

    public static Typeface selectTypeface(Context context, int textStyle) {
        String RobotoPrefix = "fonts/";
        String font;
        switch (textStyle) {
            case 0:
                font = FontUtils.FONT_REGULAR;
                break;
            case 1:
                font = FontUtils.FONT_BOLD;
                break;
            case 3:
            case 5:
                font = FontUtils.FONT_LIGHT;
                break;
            case 4:
                font = FontUtils.FONT_MEDIUM;
                break;
            case 6:
                font = FontUtils.DYSPEPSIA;
                break;
            case 7:
                font = FontUtils.FONT_AWESOME;
                break;
            case 8:
                return Typeface.DEFAULT;
            default:
                font = FontUtils.FONT_REGULAR;
                break;
        }
        return FontUtils.getTypeface(context, RobotoPrefix + font);
    }

}
