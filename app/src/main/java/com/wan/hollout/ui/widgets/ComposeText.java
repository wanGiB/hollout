package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.wan.hollout.utils.HolloutPreferences;

public class ComposeText extends com.vanniktech.emoji.EmojiEditText {

    private SpannableString hint;
    private SpannableString subHint;

    public ComposeText(Context context) {
        super(context);
    }

    public ComposeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!TextUtils.isEmpty(hint)) {
            if (!TextUtils.isEmpty(subHint)) {
                setHint(new SpannableStringBuilder().append(ellipsizeToWidth(hint))
                        .append("\n")
                        .append(ellipsizeToWidth(subHint)));
            } else {
                setHint(ellipsizeToWidth(hint));
            }
        }
    }

    private CharSequence ellipsizeToWidth(CharSequence text) {
        return TextUtils.ellipsize(text,
                getPaint(),
                getWidth() - getPaddingLeft() - getPaddingRight(),
                TruncateAt.END);
    }

    public void setHint(@NonNull String hint, @Nullable CharSequence subHint) {
        this.hint = new SpannableString(hint);
        this.hint.setSpan(new RelativeSizeSpan(0.8f), 0, hint.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        if (subHint != null) {
            this.subHint = new SpannableString(subHint);
            this.subHint.setSpan(new RelativeSizeSpan(0.8f), 0, subHint.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            this.subHint = null;
        }

        if (this.subHint != null) {
            super.setHint(new SpannableStringBuilder().append(ellipsizeToWidth(this.hint))
                    .append("\n")
                    .append(ellipsizeToWidth(this.subHint)));
        } else {
            super.setHint(ellipsizeToWidth(this.hint));
        }
    }

    public void appendInvite(String invite) {
        if (!TextUtils.isEmpty(getText()) && !getText().toString().equals(" ")) {
            append(" ");
        }

        append(invite);
        setSelection(getText().length());
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        InputConnection inputConnection = super.onCreateInputConnection(editorInfo);
        if (HolloutPreferences.isEnterSendsEnabled()) {
            editorInfo.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }

        if (Build.VERSION.SDK_INT < 21) return inputConnection;
        if (inputConnection == null) return null;

        return InputConnectionCompat.createWrapper(inputConnection, editorInfo, new CommitContentListener());
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR2)
    private static class CommitContentListener implements InputConnectionCompat.OnCommitContentListener {

        private static final String TAG = CommitContentListener.class.getName();

        private CommitContentListener() {

        }

        @Override
        public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
            if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputContentInfo.requestPermission();
                } catch (Exception e) {
                    Log.w(TAG, e);
                    return false;
                }
            }
            return false;
        }
    }

}
