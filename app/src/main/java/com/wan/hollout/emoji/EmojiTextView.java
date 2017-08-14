package com.wan.hollout.emoji;

import android.content.Context;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.ViewUtil;


public class EmojiTextView extends AppCompatTextView {

    private CharSequence source;
    private boolean needsEllipsizing;

    public EmojiTextView(Context context) {
        super(context);
    }

    public EmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(@Nullable CharSequence text, TextView.BufferType type) {
        if (useSystemEmoji()) {
            super.setText(text, type);
            return;
        }

        source = EmojiProvider.getInstance(getContext()).emojify(text, this);
        setTextEllipsized(source);
    }

    private boolean useSystemEmoji() {
        return HolloutPreferences.isSystemEmojiPreferred();
    }

    private void setTextEllipsized(final @Nullable CharSequence source) {
        super.setText(needsEllipsizing ? ViewUtil.ellipsize(source, this) : source, TextView.BufferType.SPANNABLE);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        if (drawable instanceof EmojiProvider.EmojiDrawable) invalidate();
        else super.invalidateDrawable(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int size = View.MeasureSpec.getSize(widthMeasureSpec);
        final int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        if (!useSystemEmoji() &&
                getEllipsize() == TruncateAt.END &&
                !TextUtils.isEmpty(source) &&
                (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) &&
                getPaint().breakText(source, 0, source.length() - 1, true, size, null) != source.length()) {
            needsEllipsizing = true;
            FontMetricsInt font = getPaint().getFontMetricsInt();
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(Math.abs(font.top - font.bottom), View.MeasureSpec.EXACTLY));
        } else {
            needsEllipsizing = false;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed && !useSystemEmoji()) setTextEllipsized(source);
        super.onLayout(changed, left, top, right, bottom);
    }
}
