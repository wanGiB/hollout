package com.wan.hollout.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unused")
public class ChatBubbleLayout extends FrameLayout {

    //Rect lastLineSpec = new Rect();

    public ChatBubbleLayout(Context context) {
        super(context);
    }

    public ChatBubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatBubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ChatBubbleLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        TextView childTextView = (TextView) getChildAt(0);
        View childDateView = getChildAt(1);

        int view_width = View.MeasureSpec.getSize(widthMeasureSpec);

        int lineCount = childTextView.getLineCount();

        int dateViewHeight = childDateView.getMeasuredHeight();

        int dateViewWidth = childDateView.getMeasuredWidth();

        int textViewPadding = childTextView.getPaddingLeft() + childTextView.getPaddingRight();

        int lastLineStart = childTextView.getLayout().getLineStart(lineCount - 1);
        int lastLineEnd = childTextView.getLayout().getLineEnd(lineCount - 1);

        int lastLineWidth = (int) Layout.getDesiredWidth(childTextView.getText().subSequence(lastLineStart,
                lastLineEnd), childTextView.getPaint());

        int finalFrameLayoutWidth;
        int finalFrameLayoutHeight;
        int viewPaddingLeftNRight = getPaddingLeft() + getPaddingRight();
        int finalFrameLayoutRequiredWidth = lastLineWidth + textViewPadding + dateViewWidth  + viewPaddingLeftNRight;

        int lineHeight = (childTextView.getMeasuredHeight() / lineCount) / 2;
        int bottomMargin = lineHeight - (dateViewHeight/2);

        if(( (childTextView.getMeasuredWidth() + viewPaddingLeftNRight)>= view_width)
                || (finalFrameLayoutRequiredWidth >= view_width) ) {
            finalFrameLayoutWidth = view_width;
            finalFrameLayoutHeight = getMeasuredHeight();
            if((finalFrameLayoutRequiredWidth >= view_width) ) {
                finalFrameLayoutHeight += dateViewHeight;
                finalFrameLayoutWidth = childTextView.getMeasuredWidth() + viewPaddingLeftNRight;

                ((LayoutParams)childDateView.getLayoutParams()).bottomMargin = 0;
            } else {
                ((LayoutParams)childDateView.getLayoutParams()).bottomMargin = bottomMargin;
            }

        } else {
            finalFrameLayoutWidth = Math.max(finalFrameLayoutRequiredWidth,
                    childTextView.getMeasuredWidth() + viewPaddingLeftNRight);
            finalFrameLayoutHeight = getMeasuredHeight();
            ((LayoutParams)childDateView.getLayoutParams()).bottomMargin = bottomMargin;
        }

        if(finalFrameLayoutWidth > view_width)
            finalFrameLayoutWidth = view_width;

        setMeasuredDimension(finalFrameLayoutWidth, finalFrameLayoutHeight);
    }
}
