package com.wan.hollout.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Wan Clem
 */

public class UiUtils {

    public static Handler handler = new Handler(Looper.getMainLooper());
    private static String TAG = "UiUtils";

    public static void showSafeToast(final String toastMessage) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ApplicationLoader.getInstance(), toastMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void runOnMain(final @NonNull Runnable runnable) {
        if (isMainThread()) runnable.run();
        else handler.post(runnable);
    }

    public static void blinkView(View mView) {
        try {
            Animation mFadeInFadeIn = getAnimation(ApplicationLoader.getInstance(), android.R.anim.fade_in);
            mFadeInFadeIn.setRepeatMode(Animation.REVERSE);
            animateView(mView, mFadeInFadeIn);
        } catch (IllegalStateException | NullPointerException ignored) {

        }
    }

    public static ProgressDialog operationsProgressDialog;

    public static void showProgressDialog(final Context context, final String message) {
        operationsProgressDialog = new ProgressDialog(context);
        operationsProgressDialog.setCancelable(false);
        operationsProgressDialog.setMessage(message);
        operationsProgressDialog.show();
    }

    public static void dismissProgressDialog() {
        try {
            if (operationsProgressDialog != null) {
                if (operationsProgressDialog.isShowing()) {
                    operationsProgressDialog.dismiss();
                }
            }
        } catch (WindowManager.BadTokenException e) {
            HolloutLogger.d(TAG, "Your father lip. Dialog no gree close again o. See the error na = " + e.getMessage());
        }
    }

    /***
     * Toggles a view visibility state
     *
     * @param view The view to toggle
     * @param show Flag indicating whether a view should be setVisible or not
     **/
    public static void showView(View view, boolean show) {
        if (view != null) {
            if (show) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                    view.invalidate();
                }
            } else {
                if (view.getVisibility() != View.GONE) {
                    view.setVisibility(View.GONE);
                    view.invalidate();
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /***
     * Toggles a ViewFlipper's Displayed Child's state
     *
     * @param viewFlipper The viewflipper to toggle
     * @param child       The Child to switch to
     **/
    public static void toggleFlipperState(ViewFlipper viewFlipper, int child) {
        if (viewFlipper != null) {
            if (viewFlipper.getDisplayedChild() != child) {
                viewFlipper.setDisplayedChild(child);
            }
        }
    }

    public static void setUpRefreshColorSchemes(Context context, SwipeRefreshLayout mSwipeRefreshLayout) {
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.hollout_color_one),
                ContextCompat.getColor(context, R.color.hollout_color_two),
                ContextCompat.getColor(context, R.color.hollout_color_three),
                ContextCompat.getColor(context, R.color.hollout_color_four),
                ContextCompat.getColor(context, R.color.hollout_color_five));
    }

    public static String getTimeAgo(Date past) {
        Date now = new Date();
        String time;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
        if (minutes <= 59) {
            if (minutes < 1) {
                time = "a minute ago";
            } else if (minutes == 1) {
                time = "just now";
            } else {
                time = minutes + " mins ago";
            }
        } else {
            long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            if (hours <= 24) {
                if (hours == 24) {
                    time = "an hour ago";
                } else {
                    time = hours + " hours ago";
                }
            } else {
                if (hours <= 48) {
                    time = "yesterday, " + AppConstants.DATE_FORMATTER_IN_12HRS.format(past);
                } else {
                    String currentYear = AppConstants.DATE_FORMATTER_IN_YEARS.format(now);
                    time = StringUtils.strip(AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(past), currentYear);
                }
            }
        }
        return time;
    }

    public static Animation getAnimation(Context context, int animationId) {
        return AnimationUtils.loadAnimation(context, animationId);
    }

    public static synchronized void animateView(View view, Animation animation) {
        if (view != null) {
            view.startAnimation(animation);
        }
    }

    public static void loadImage(final Activity context, final String photoPath, final ImageView imageView) {
        if (imageView != null) {
            if (isNotEmpty(photoPath)) {
                if (context != null) {
                    if (Build.VERSION.SDK_INT >= 17) {
                        if (!context.isDestroyed()) {
                            Glide.with(context).load(photoPath).listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    imageView.setImageResource(R.drawable.web_hi_res_512);
                                    HolloutLogger.d(TAG, "An exception was raised while loading an image");
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(imageView);
                            imageView.invalidate();
                        }
                    } else {
                        Glide.with(context).load(photoPath).listener(new RequestListener<String, GlideDrawable>() {

                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                               /* if (org.parceler.apache.commons.lang.StringUtils.isNotEmpty(placeHolderName)) {
                                    loadUserNameInitialsIntoBitmap(placeHolderName, imageView);
                                }*/
                                imageView.setImageResource(R.mipmap.ic_launcher);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }

                        }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(imageView);
                        imageView.invalidate();
                    }
                }
            }
        }
    }

}
