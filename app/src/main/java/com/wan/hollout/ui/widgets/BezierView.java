package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;

import com.wan.hollout.R;

@SuppressLint("ViewConstructor")
public class BezierView extends RelativeLayout {

    private Paint paint;
    private Path path;
    private int bezierWidth, bezierHeight;
    private int backgroundColor;
    private Context context;

    public BezierView(Context context, int backgroundColor) {
        super(context);
        this.context = context;
        this.backgroundColor = backgroundColor;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setBackgroundColor(ContextCompat.getColor(context, R.color.butter_bar_transparent));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * Set paint color to fill view
         */
        paint.setColor(backgroundColor);
        /**
         * Reset path before drawing
         */
        path.reset();
        /**
         * Start point for drawing
         */
        path.moveTo(0, bezierHeight);
        /**
         * Seth half path of bezier view
         */
        path.cubicTo(bezierWidth / 4, bezierHeight, bezierWidth / 4, 0, bezierWidth / 2, 0);
        /**
         * Seth second part of bezier view
         */
        path.cubicTo((bezierWidth / 4) * 3, 0, (bezierWidth / 4) * 3, bezierHeight, bezierWidth, bezierHeight);
        /**
         * Draw our bezier view
         */
        canvas.drawPath(path, paint);
    }

    /**
     * Build bezier view with given width and height
     *
     * @param bezierWidth  Given width
     * @param bezierHeight Given height
     */
    public void build(int bezierWidth, int bezierHeight) {
        this.bezierWidth = bezierWidth;
        this.bezierHeight = bezierHeight;
    }

    /**
     * Change bezier view background color
     *
     * @param backgroundColor Target color
     */
    public void changeBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

}

