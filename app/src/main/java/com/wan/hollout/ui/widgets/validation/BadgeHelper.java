
package com.wan.hollout.ui.widgets.validation;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.utils.UiUtils;


public class BadgeHelper {

    /**
     * Show badge
     *
     * @param view      target badge
     * @param badgeItem BadgeItem object
     */
    public static void showBadge(RelativeLayout view, BadgeItem badgeItem, boolean shouldShowBadgeWithNinePlus) {
        UiUtils.showView(view, false);
        TextView badgeTextView = (TextView) view.findViewById(R.id.badge_text_view);
        if (shouldShowBadgeWithNinePlus)
            badgeTextView.setText(badgeItem.getBadgeText());
        else
            badgeTextView.setText(badgeItem.getFullBadgeText());

        view.setScaleX(0);
        view.setScaleY(0);

        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(1)
                .scaleY(1)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        UiUtils.showView(view, true);
                    }
                })
                .start();
    }

    /**
     * Show badge
     *
     * @param view target badge
     */
    public static void hideBadge(View view) {
        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(0)
                .scaleY(0)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final View view) {
                        UiUtils.showView(view, false);
                    }
                })
                .start();
    }

    /**
     * Force show badge without animation
     *
     * @param view      target budge
     * @param badgeItem BadgeItem object
     */
    public static void forceShowBadge(RelativeLayout view, BadgeItem badgeItem, boolean shouldShowBadgeWithNinePlus) {
        UiUtils.showView(view, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(makeShapeDrawable(badgeItem.getBadgeColor()));
        } else {
            view.setBackgroundDrawable(makeShapeDrawable(badgeItem.getBadgeColor()));
        }
        TextView badgeTextView = (TextView) view.findViewById(R.id.badge_text_view);
        if (shouldShowBadgeWithNinePlus)
            badgeTextView.setText(badgeItem.getBadgeText());
        else
            badgeTextView.setText(badgeItem.getFullBadgeText());
    }

    /**
     * Make circle drawable for badge background
     *
     * @param color background color
     * @return return colored circle drawable
     */
    public static ShapeDrawable makeShapeDrawable(int color) {
        ShapeDrawable badgeBackground = new ShapeDrawable(new OvalShape());
        badgeBackground.setIntrinsicWidth(10);
        badgeBackground.setIntrinsicHeight(10);
        badgeBackground.getPaint().setColor(color);
        return badgeBackground;
    }

}
