package com.wan.hollout.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Wan Clem
 */

public class HolloutUtils {

    public static String stripDollar(String string) {
        return StringUtils.replace(string, "$", "USD");
    }

}
