package com.wan.hollout.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Wan Clem
 */

public class HolloutUtils {

    public static float density = 1;

    public static String stripDollar(String string) {
        return StringUtils.replace(string, "$", "USD");
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

}
