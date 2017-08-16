package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


import com.wan.hollout.R;
import com.wan.hollout.emoji.EmojiTextView;
import com.wan.hollout.eventbuses.TypingFinishedBus;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.TypingSimulationConstants;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;

/**
 * @author Wan Clem
 */

public class HolloutTextView extends EmojiTextView {

    public static final int INVALIDATE = 0x767;
    private Random random;
    private CharSequence mText;
    private Handler handler;
    private int charIncrease;
    private int typerSpeed;

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
            int textStyle = 0, textAnimationStyle = -1;
            if (a.hasValue(R.styleable.HolloutTextView_textStyle)) {
                textStyle = a.getInt(R.styleable.HolloutTextView_textStyle, 0);
            } else {
                //use default schema
                textStyle = attrs.getAttributeIntValue(DEFAULT_SCHEMA, "textStyle", 0);
            }
            if (a.hasValue(R.styleable.HolloutTextView_text_animationStyle)) {
                textAnimationStyle = a.getInt(R.styleable.HolloutTextView_text_animationStyle, 0);
                typerSpeed = a.getInt(R.styleable.HolloutTextView_typerSpeed, 100);
                charIncrease = a.getInt(R.styleable.HolloutTextView_charIncrease, 2);
            }
            a.recycle();
            applyCustomFont(context, textStyle);
            if (textAnimationStyle != -1 && !isInEditMode()) {
                applyTypingState();
            }
        }
    }

    private void applyTypingState() {
        random = new Random();
        mText = getText();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int currentLength = getText().length();
                if (currentLength < mText.length()) {
                    if (currentLength + charIncrease > mText.length()) {
                        charIncrease = mText.length() - currentLength;
                    }
                    append(mText.subSequence(currentLength, currentLength + charIncrease));
                    long randomTime = typerSpeed + random.nextInt(typerSpeed);
                    Message message = Message.obtain();
                    message.what = INVALIDATE;
                    handler.sendMessageDelayed(message, randomTime);
                    return false;
                }
                if (TypingSimulationConstants.CURRENTLY_TYPED_WORD.trim().equals(getText().toString().trim())) {
                    EventBus.getDefault().post(new TypingFinishedBus(true));
                }
                return false;
            }
        });
    }

    public int getTyperSpeed() {
        return typerSpeed;
    }

    public void setTyperSpeed(int typerSpeed) {
        this.typerSpeed = typerSpeed;
    }

    public int getCharIncrease() {
        return charIncrease;
    }

    public void setCharIncrease(int charIncrease) {
        this.charIncrease = charIncrease;
    }

    private void applyCustomFont(Context context, int textStyle) {
        Typeface typeface = FontUtils.selectTypeface(context, textStyle);
        if (typeface != null) {
            setTypeface(typeface);
        }
    }

    public void setProgress(float progress) {
        setText(mText.subSequence(0, (int) (mText.length() * progress)));
    }

    public void animateText(CharSequence text) {
        if (text == null) {
            throw new RuntimeException("text must not  be null");
        }
        mText = text;
        setText("");
        Message message = Message.obtain();
        message.what = INVALIDATE;
        handler.sendMessage(message);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeMessages(INVALIDATE);
        }
    }

}
