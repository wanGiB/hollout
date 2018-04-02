package com.wan.hollout.ui.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.wan.hollout.utils.HolloutLogger;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wan Clem
 */

public class HolloutTextFormatter {

    private static RichText.Flag boldFlag = new RichText.Flag(
            RichText.INVALID_INDEX,
            RichText.INVALID_INDEX,
            RichText.BOLD_FLAG);

    private static List<RichText.Flag> boldFlags = new ArrayList<>();

    public static void applyRichTextFormatting(Editable editable) {
        String richRawString = editable.toString();
        if (StringUtils.isNotEmpty(richRawString)) {
            char[] chars = richRawString.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == RichText.BOLD_FLAG) {
                    if (boldFlag.start == RichText.INVALID_INDEX) {
                        boldFlag.start = i;
                        HolloutLogger.d("CharLogger", "Bold Start = " + boldFlag.start);
                    } else {
                        if (boldFlag.end == RichText.INVALID_INDEX) {
                            boldFlag.end = i;
                            HolloutLogger.d("CharLogger", "Bold End = " + boldFlag.end);
                            boldFlags.add(boldFlag);
                            /*int safeStart = boldFlag.start - 1 > 0 ? boldFlag.start - 1 : 1;
                            editable.setSpan(new ForegroundColorSpan(Color.GRAY), safeStart - 1, safeStart + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            editable.setSpan(new ForegroundColorSpan(Color.GRAY), boldFlag.end - 1, boldFlag.end - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            */
                            boldFlag.start = RichText.INVALID_INDEX;
                            boldFlag.end = RichText.INVALID_INDEX;
                        }
                    }
                }
            }
            applyBoldFormatting(editable);
        }

    }

    private static void applyBoldFormatting(Editable editable) {
        if (!boldFlags.isEmpty()) {
            for (RichText.Flag boldFlag : boldFlags) {
                StyleSpan bss = new StyleSpan(Typeface.BOLD);
                editable.setSpan(bss, getBoldStart(boldFlag), getBoldEndFlag(boldFlag), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static int getBoldEndFlag(RichText.Flag boldFlag) {
        return boldFlag.end > boldFlag.start ? boldFlag.end : boldFlag.start;
    }

    private static int getBoldStart(RichText.Flag boldFlag) {
        return boldFlag.start > boldFlag.end ? boldFlag.end : boldFlag.start;
    }

}
