package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.vanniktech.emoji.EmojiTextView;
import com.wan.hollout.R;
import com.wan.hollout.utils.FontUtils;

/**
 * @author Wan Clem
 */

public class HolloutTextView extends EmojiTextView {

    private static final String DEFAULT_SCHEMA = "xmlns:android=\"http://schemas.android.com/apk/res/android\"";

    public HolloutTextView(Context context) {
        super(context);
    }

    public HolloutTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HolloutTextView);
            int textStyle;
            if (a.hasValue(R.styleable.HolloutTextView_textStyle)) {
                textStyle = a.getInt(R.styleable.HolloutTextView_textStyle, 0);
            } else {
                //use default schema
                textStyle = attrs.getAttributeIntValue(DEFAULT_SCHEMA, "textStyle", 0);
            }
            a.recycle();
            applyCustomFont(context, textStyle);
        }
    }

    public void applyCustomFont(Context context, int textStyle) {
        Typeface typeface = FontUtils.selectTypeface(context, textStyle);
        if (typeface != null) {
            setTypeface(typeface);
        }
    }

}
