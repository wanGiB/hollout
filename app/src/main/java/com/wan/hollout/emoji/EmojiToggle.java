package com.wan.hollout.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.wan.hollout.R;

public class EmojiToggle extends android.support.v7.widget.AppCompatImageView implements EmojiDrawer.EmojiDrawerListener {

    private Drawable emojiToggle;
    private Drawable imeToggle;
    private EmojiDrawer emojiDrawer;

    public EmojiToggle(Context context) {
        super(context);
        initialize();
    }

    public EmojiToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EmojiToggle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setToEmoji() {
        setImageDrawable(emojiToggle);
    }

    public void setToIme() {
        setImageDrawable(imeToggle);
    }

    private void initialize() {
        int attributes[] = new int[]{R.attr.conversation_emoji_toggle,
                R.attr.conversation_keyboard_toggle};

        TypedArray drawables = getContext().obtainStyledAttributes(attributes);
        this.emojiToggle = drawables.getDrawable(0);
        this.imeToggle = drawables.getDrawable(1);

        drawables.recycle();
        setToEmoji();
    }

    public void attach(EmojiDrawer drawer) {
        this.emojiDrawer = drawer;
        drawer.setDrawerListener(this);
    }

    @Override
    public void onShown() {
        setToIme();
    }

    @Override
    public void onHidden() {
        setToEmoji();
    }

    public EmojiDrawer.EmojiDrawerListener getEmojiDrawerListener(){
        return this;
    }

}
