package com.wan.hollout.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wan.hollout.components.ApplicationLoader;

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
