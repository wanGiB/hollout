package com.wan.hollout.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.interfaces.ListenableFuture;
import com.wan.hollout.listeners.NestedViewHideShowScrollListener;
import com.wan.hollout.listeners.RecyclerViewHideScrollListener;
import com.wan.hollout.ui.widgets.CircularProgressButton;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unused")
public class UiUtils {

    public static Handler handler = new Handler(Looper.getMainLooper());
    private static String TAG = "UiUtils";

    private static ColorGenerator generator = ColorGenerator.MATERIAL;

    private static ApplicationLoader mContext = ApplicationLoader.getInstance();

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

    public static synchronized ProgressDialog showProgressDialog(final Activity context, final String message) {
        if (Build.VERSION.SDK_INT >= 17 && !context.isDestroyed()) {
            try {
                return constructProgressDialog(context, message);
            } catch (WindowManager.BadTokenException ignore) {

            }
        } else {
            try {
                return constructProgressDialog(context, message);
            } catch (WindowManager.BadTokenException ignore) {

            }
        }
        return null;
    }

    @NonNull
    private static ProgressDialog constructProgressDialog(Activity context, String message) {
        ProgressDialog operationsProgressDialog = new ProgressDialog(context);
        operationsProgressDialog.setCancelable(false);
        operationsProgressDialog.setMessage(message);
        operationsProgressDialog.show();
        return operationsProgressDialog;
    }

    public static synchronized void dismissProgressDialog(ProgressDialog operationsProgressDialog) {
        try {
            if (operationsProgressDialog != null) {
                if (operationsProgressDialog.isShowing()) {
                    operationsProgressDialog.dismiss();
                    operationsProgressDialog.cancel();
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

    public static void dismissKeyboard(View trigger) {
        InputMethodManager imm = (InputMethodManager) ApplicationLoader.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(trigger.getWindowToken(), 0);
        }
    }

    public static void loadImage(final Activity context, final String photoPath, final ImageView imageView) {
        if (imageView != null) {
            if (isNotEmpty(photoPath)) {
                Glide.with(ApplicationLoader.getInstance()).load(photoPath).listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        imageView.setImageResource(R.mipmap.ic_launcher);
                        HolloutLogger.d(TAG, "An exception was raised while loading an image. Error Message = "
                                + (e != null && e.getMessage() != null ? e.getMessage() : "") + " For Photo Url = " + photoPath);
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

    public static synchronized void removeAllDrawablesFromTextView(TextView textView) {
        if (textView != null) {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            textView.invalidate();
        }
    }

    public static synchronized void attachDrawableToTextView(Context context, TextView textView, int resource, DrawableDirection direction) {
        if (textView != null) {
            Drawable drawableToAttach = ContextCompat.getDrawable(context, resource);
            if (direction == DrawableDirection.LEFT) {
                textView.setCompoundDrawablesWithIntrinsicBounds(drawableToAttach, null, null, null);
            } else if (direction == DrawableDirection.RIGHT) {
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableToAttach, null);
            } else if (direction == DrawableDirection.BOTTOM) {
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawableToAttach);
            } else if (direction == DrawableDirection.TOP) {
                textView.setCompoundDrawablesWithIntrinsicBounds(null, drawableToAttach, null, null);
            }
            textView.invalidate();
        }
    }

    public static Spannable highlightTextIfNecessary(String search, String originalText, int color) {
        if (isNotEmpty(search)) {
            if (containsIgnoreCase(originalText, search.trim())) {
                int startPost = indexOfIgnoreCase(originalText, search.trim());
                int endPost = startPost + search.length();
                Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
                if (startPost != -1) {
                    spanText.setSpan(new ForegroundColorSpan(color), startPost, endPost, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return spanText;
                } else {
                    return new SpannableString(originalText);
                }
            } else {
                return new SpannableString(originalText);
            }

        } else {
            return new SpannableString(originalText);
        }
    }

    public static Spannable highlightTextIfNecessary(String search, Spanned originalText, int color) {
        try {
            if (isNotEmpty(search)) {
                if (containsIgnoreCase(originalText, search.trim())) {
                    int startPost = indexOfIgnoreCase(originalText, search.trim());
                    int endPost = startPost + search.length();
                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
                    if (startPost != -1) {
                        spanText.setSpan(new ForegroundColorSpan(color), startPost, endPost, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return spanText;
                    } else {
                        return new SpannableString(originalText);
                    }
                } else {
                    return new SpannableString(originalText);
                }

            } else {
                return new SpannableString(originalText);
            }
        } catch (IndexOutOfBoundsException e) {
            return new SpannableString(originalText);
        }
    }

    public enum DrawableDirection {
        LEFT,
        RIGHT,
        BOTTOM,
        TOP
    }

    public static boolean canShowStatus(ParseObject parseUser, int type, HashMap<String, Object> optionalProps) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String signedInUserStatusVisibility = signedInUser.getString(AppConstants.STATUS_VISIBILITY_PREF);
            String statusVisibilityOfOtherUser = parseUser != null ? parseUser.getString(AppConstants.STATUS_VISIBILITY_PREF) : (String) optionalProps.get(AppConstants.STATUS_VISIBILITY_PREF);

            //The user status is Anyone..Lets check to see if the current user is hiding his/her status..So we know how to deal with him
            if (type == AppConstants.ENTITY_TYPE_CLOSEBY) {
                return (statusVisibilityOfOtherUser.equals(mContext.getString(R.string.anyone))
                        || statusVisibilityOfOtherUser.equals(mContext.getString(R.string.only_closebies)))
                        && signedInUserStatusVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserStatusVisibility.equals(mContext.getString(R.string.only_closebies));
            } else {
                return (statusVisibilityOfOtherUser.equals(mContext.getString(R.string.anyone))
                        || statusVisibilityOfOtherUser.equals(mContext.getString(R.string.only_chats)))
                        && (signedInUserStatusVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserStatusVisibility.equals(mContext.getString(R.string.only_chats)));
            }
        }
        return true;
    }

    public static boolean canShowLocation(ParseObject parseUser, int entityType, HashMap<String, Object> optionalProps) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String signedInUserLocationVisibility = signedInUser.getString(AppConstants.LOCATION_VISIBILITY_PREF);
            String locationVisibilityOfOtherUser = parseUser != null ? parseUser.getString(AppConstants.LOCATION_VISIBILITY_PREF) : (String) optionalProps.get(AppConstants.LOCATION_VISIBILITY_PREF);

            //The user status is Anyone..Lets check to see if the current user is hiding his/her status..So we know how to deal with him
            if (entityType == AppConstants.ENTITY_TYPE_CLOSEBY) {
                return (locationVisibilityOfOtherUser.equals(mContext.getString(R.string.anyone))
                        || locationVisibilityOfOtherUser.equals(mContext.getString(R.string.only_closebies)))
                        && (signedInUserLocationVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserLocationVisibility.equals(mContext.getString(R.string.only_closebies)));
            } else {
                return (locationVisibilityOfOtherUser.equals(mContext.getString(R.string.anyone))
                        || locationVisibilityOfOtherUser.equals(mContext.getString(R.string.only_chats)))
                        && (signedInUserLocationVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserLocationVisibility.equals(mContext.getString(R.string.only_chats)));
            }
        }
        return false;
    }

    public static boolean canShowPresence(ParseObject parseUser, int type, HashMap<String, Object> optionalProps) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String signedInUserLastSeenVisibility = signedInUser.getString(AppConstants.LAST_SEEN_VISIBILITY_PREF);
            String lastSeenVisibilityOtherUser = parseUser != null ? parseUser.getString(AppConstants.LAST_SEEN_VISIBILITY_PREF) :
                    (String) optionalProps.get(AppConstants.LAST_SEEN_VISIBILITY_PREF);

            //The user status is Anyone..Lets check to see if the current user is hiding his/her status..So we know how to deal with him
            if (type == AppConstants.ENTITY_TYPE_CLOSEBY) {
                return (lastSeenVisibilityOtherUser.equals(mContext.getString(R.string.anyone))
                        || lastSeenVisibilityOtherUser.equals(mContext.getString(R.string.only_closebies)))
                        && (signedInUserLastSeenVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserLastSeenVisibility.equals(mContext.getString(R.string.only_closebies)));
            } else {
                return (lastSeenVisibilityOtherUser.equals(mContext.getString(R.string.anyone))
                        || lastSeenVisibilityOtherUser.equals(mContext.getString(R.string.only_chats)))
                        && signedInUserLastSeenVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserLastSeenVisibility.equals(mContext.getString(R.string.only_chats));
            }
        }
        return false;
    }

    public static boolean canShowAge(ParseObject parseUser, int type, HashMap<String, Object> optionalProps) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String signedInUserAgeVisibility = signedInUser.getString(AppConstants.AGE_VISIBILITY_PREF);
            String ageVisibilityOfOtherUser = parseUser != null ? parseUser.getString(AppConstants.AGE_VISIBILITY_PREF) :
                    (String) optionalProps.get(AppConstants.AGE_VISIBILITY_PREF);
            //The user status is Anyone..Lets check to see if the current user is hiding his/her status..So we know how to deal with him
            if (type == AppConstants.ENTITY_TYPE_CLOSEBY) {
                return (ageVisibilityOfOtherUser.equals(mContext.getString(R.string.anyone))
                        || ageVisibilityOfOtherUser.equals(mContext.getString(R.string.only_closebies)))
                        && (signedInUserAgeVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserAgeVisibility.equals(mContext.getString(R.string.only_closebies)));
            } else {
                return (ageVisibilityOfOtherUser.equals(mContext.getString(R.string.anyone))
                        || ageVisibilityOfOtherUser.equals(mContext.getString(R.string.only_chats)))
                        && signedInUserAgeVisibility.equals(mContext.getString(R.string.anyone))
                        || signedInUserAgeVisibility.equals(mContext.getString(R.string.only_chats));
            }
        }
        return false;
    }

    public static synchronized void setTextOnView(final TextView holloutTextView, final Spanned message) {
        if (holloutTextView != null) {
            if (isNotEmpty(message)) {
                holloutTextView.setText(message);
                holloutTextView.invalidate();
            }
        }
    }

    public static synchronized void setTextOnView(final TextView textView, final String message) {
        if (textView != null) {
            if (isNotEmpty(message)) {
                textView.setText(message);
                textView.invalidate();
            }
        }
    }

    public static String getDaysAgo(String dateString) {

        Context context = ApplicationLoader.getInstance();

        String todayDateStringValue = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(new Date());

        String[] slittedChatDate = dateString.split("\\s+");
        String[] todayDateSplit = todayDateStringValue.split("\\s+");

        if (dateString.equals(todayDateStringValue)) {
            return context.getString(R.string.today);
        } else {
            int dayOfMessageMonth = Integer.parseInt(slittedChatDate[1].trim());
            String monthOfMessage = slittedChatDate[2].trim();
            String yearOfMessage = slittedChatDate[3].trim();

            int currentDayOfCurrentMonth = Integer.parseInt(todayDateSplit[1].trim());
            String currentMonth = todayDateSplit[2].trim();
            String currentYear = todayDateSplit[3].trim();

            if (monthOfMessage.equals(currentMonth) && yearOfMessage.equals(currentYear)) {
                if (currentDayOfCurrentMonth == (dayOfMessageMonth + 1)) {
                    return context.getString(R.string.yesterday);
                } else {
                    return dateString;
                }
            } else {
                return dateString;
            }
        }
    }

    public static String getLastSeen(long longTime) {
        Date date = new Date(longTime);
        String birthDayStyle = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(date);
        String twelveHrStyle = AppConstants.DATE_FORMATTER_IN_12HRS.format(date);
        return "Active: " + StringUtils.capitalize(getDaysAgo(birthDayStyle)) + " at " + twelveHrStyle;
    }


    /**
     * Change given image view tint
     *
     * @param imageView target image view
     * @param color     tint color
     */
    public static void tintImageView(ImageView imageView, int color) {
        imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static void tintImageViewNoMode(ImageView imageView, int color) {
        imageView.setColorFilter(color);
    }

    public static void showKeyboard(View trigger) {
        InputMethodManager imm = (InputMethodManager) ApplicationLoader.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(trigger, InputMethodManager.SHOW_FORCED);
    }

    public static void snackMessage(String message, View anchorView, boolean shortDuration, String actionMessage, final DoneCallback<Object> actionCallback) {
        if (anchorView != null) {
            Snackbar snackbar = Snackbar.make(anchorView, message, actionMessage != null ? Snackbar.LENGTH_INDEFINITE : (shortDuration ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG));
            if (actionCallback != null) {
                snackbar.setAction(actionMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionCallback.done(null, null);
                    }
                });
            }
            snackbar.show();
        }
    }

    public static synchronized void morphRequestToSuccess(
            final CircularProgressButton circularProgressButton) {
        circularProgressButton.setProgress(100);
        circularProgressButton.invalidate();
    }

    public static synchronized void morphRequestToProgress(
            final CircularProgressButton circularProgressButton) {
        circularProgressButton.setProgress(10);
    }

    private static synchronized void morphRequestToError(
            final CircularProgressButton circularProgressButton) {
        circularProgressButton.setProgress(-1);
        circularProgressButton.invalidate();
    }

    public static synchronized void morphRequestToIdle(CircularProgressButton circularProgressButton) {
        circularProgressButton.setProgress(0);
        circularProgressButton.invalidate();
    }

    public interface AnimationListener {
        /**
         * @return true to override parent. Else execute Parent method
         */
        boolean onAnimationStart(View view);

        boolean onAnimationEnd(View view);

        boolean onAnimationCancel(View view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void reveal(final View view, final AnimationListener listener) {
        try {
            if (view != null) {
                int cx = view.getWidth() - (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 24, view.getResources().getDisplayMetrics());
                int cy = view.getHeight() / 2;
                int finalRadius = Math.max(view.getWidth(), view.getHeight());
                Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
                view.setVisibility(View.VISIBLE);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (listener != null) {
                            listener.onAnimationStart(view);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (listener != null) {
                            listener.onAnimationEnd(view);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (listener != null) {
                            listener.onAnimationCancel(view);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }

                });

                anim.start();

            }

        } catch (IllegalStateException | NullPointerException ignored) {

        }

    }

    public static void fadeInView(View view, int duration, final AnimationListener listener) {
        showView(view, true);
        view.setAlpha(0f);
        ViewPropertyAnimatorListener vpListener = null;

        if (listener != null) {
            vpListener = new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {
                    if (!listener.onAnimationStart(view)) {
                        view.setDrawingCacheEnabled(true);
                    }
                }

                @Override
                public void onAnimationEnd(View view) {
                    if (!listener.onAnimationEnd(view)) {
                        view.setDrawingCacheEnabled(false);
                    }
                }

                @Override
                public void onAnimationCancel(View view) {
                }
            };
        }
        ViewCompat.animate(view).alpha(1f).setDuration(duration).setListener(vpListener);
    }

    public static void wait(Object lock, long timeout) {
        try {
            lock.wait(timeout);
        } catch (InterruptedException ie) {
            throw new AssertionError(ie);
        }
    }

    @SuppressWarnings("unchecked")
    public static ListenableFuture<Boolean> animateOut(final @NonNull View view, final @NonNull Animation animation, final int visibility) {
        final SettableFuture future = new SettableFuture();
        if (view.getVisibility() == visibility) {
            future.set(true);
        } else {
            view.clearAnimation();
            animation.reset();
            animation.setStartTime(0);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(visibility);
                    future.set(true);
                }
            });
            view.startAnimation(animation);
        }
        return future;
    }

    public static void animateIn(final @NonNull View view, final @NonNull Animation animation) {
        if (view.getVisibility() == View.VISIBLE) return;

        view.clearAnimation();
        animation.reset();
        animation.setStartTime(0);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(animation);
    }

    private static Animation getAlphaAnimation(float from, float to, int duration) {
        final Animation anim = new AlphaAnimation(from, to);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(duration);
        return anim;
    }

    public static void previewSelectedFile(Activity activity, HolloutFile holloutFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        RoundedImageView previewImageView = new RoundedImageView(activity);
        previewImageView.setCornerRadius(5);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(700,
                700);
        previewImageView.setLayoutParams(layoutParams);
        previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        UiUtils.loadImage(activity, holloutFile.getLocalFilePath(), previewImageView);
        builder.setView(previewImageView);
        builder.create().show();
    }

    public static void previewSelectedFile(Activity activity, Uri holloutFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        RoundedImageView previewImageView = new RoundedImageView(activity);
        previewImageView.setCornerRadius(5);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(700,
                700);
        previewImageView.setLayoutParams(layoutParams);
        previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        UiUtils.loadImage(activity, holloutFile.getPath(), previewImageView);
        builder.setView(previewImageView);
        builder.create().show();
    }

    public static void loadMusicPreview(Activity activity, ImageView audioIcon, Uri uri) {
        Glide.with(ApplicationLoader.getInstance()).load(uri).error(R.drawable.x_ic_folde_music).placeholder(R.drawable.x_ic_folde_music).crossFade().into(audioIcon);
        audioIcon.invalidate();
    }

    public static void bangSound(Context context, int soundId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundId);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public static String getTimeString(long millis) {
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        return String.format(Locale.getDefault(), "%02d", minutes) +
                ":" +
                String.format(Locale.getDefault(), "%02d", seconds);
    }

    //Pull all links from the body for easy retrieval
    @SuppressWarnings("unchecked")
    public static ArrayList<String> pullLinks(String text) {
        ArrayList<String> links = new ArrayList<>();
        String regex = "\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            links.add(urlStr);
        }
        return links;
    }

    //Pull all links from the body for easy retrieval
    @SuppressWarnings("unchecked")
    public static List<String> pullBoldTags(String text) {
        String[] substringsBetweenAsteriks = StringUtils.substringsBetween(text, "*", "*");
        List<String> boldTags = new ArrayList<>();
        if (substringsBetweenAsteriks != null && substringsBetweenAsteriks.length > 0) {
            boldTags = Arrays.asList(substringsBetweenAsteriks);
        }
        return boldTags;
    }

    @SuppressWarnings("unchecked")
    public static List<String> pullItalicTags(String text) {
        String[] substringsBetweenUnderscores = StringUtils.substringsBetween(text, "_", "_");
        List<String> italicTags = new ArrayList<>();
        if (substringsBetweenUnderscores != null && substringsBetweenUnderscores.length > 0) {
            italicTags = Arrays.asList(substringsBetweenUnderscores);
        }
        return italicTags;
    }

    @SuppressWarnings("unchecked")
    public static List<String> pullStrikeThroughTags(String text) {
        String[] substringsBetweenHyphens = StringUtils.substringsBetween(text, "-", "-");
        List<String> strikeThroughTags = new ArrayList<>();
        if (substringsBetweenHyphens != null && substringsBetweenHyphens.length > 0) {
            strikeThroughTags = Arrays.asList(substringsBetweenHyphens);
        }
        return strikeThroughTags;
    }

    public static int resolveDimension(Context context, @AttrRes int attr, @DimenRes int fallbackRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getDimensionPixelSize(0, (int) context.getResources().getDimension(fallbackRes));
        } finally {
            a.recycle();
        }
    }

    @ColorInt
    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }

    public static int resolveInt(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getInt(0, fallback);
        } finally {
            a.recycle();
        }
    }

    public static String resolveString(Context context, @AttrRes int attr) {
        TypedValue v = new TypedValue();
        context.getTheme().resolveAttribute(attr, v, true);
        return (String) v.string;
    }

    public static int resolveResId(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getResourceId(0, fallback);
        } finally {
            a.recycle();
        }
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
                / 255;
        return darkness >= 0.5;
    }

    public static Drawable tintDrawable(Drawable drawable, @ColorInt int color) {
        Drawable wrapped = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrapped, color);
        return wrapped;
    }

    public static Animation getBlinkingAnimation(Context context) {
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(1500);
        return animation;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private static int[] randomColors = new int[]{R.color.hollout_color,
            R.color.hollout_color_one,
            R.color.hollout_color_two,
            R.color.hollout_color_three,
            R.color.hollout_color_four, R.color.hollout_color_five};

    public static void generateRandomBackgroundColor(View view, int position) {
        Context context = ApplicationLoader.getInstance();
        if (position == 0) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTwitter));
        } else if (position == 1) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_primary));
        } else if (position == 2) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFacebook));
        } else if (position == 3) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.gplus_color_1));
        } else if (position == 4) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.gplus_color_3));
        } else if (position == 5) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
        } else if (position == 6) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFacebook));
        } else if (position == 7) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.gplus_color_2));
        } else if (position == 8) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.gplus_color_3));
        } else if (position == 9) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        } else if (position == 10) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_100));
        } else {
            Random random = new Random();
            view.setBackgroundColor(ContextCompat.getColor(context, randomColors[random.nextInt(randomColors.length - 1)]));
        }
    }

    public static void attachViewToRecyclerViewState(RecyclerView recyclerView, final View viewToAttach) {
        recyclerView.addOnScrollListener(new RecyclerViewHideScrollListener() {
            @Override
            public void onHide() {
                UiUtils.showSafeToast("OnHide Called");
                //Hide View
                //Hide the Bottom Space NavigationView here
                Animation slideOutAnimation = AnimationUtils.loadAnimation(ApplicationLoader.getInstance(), R.anim.slide_out_bottom);
                slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showView(viewToAttach, false);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                });
                viewToAttach.startAnimation(slideOutAnimation);
            }

            @Override
            public void onShow() {
                UiUtils.showSafeToast("OnShow Called");
                showView(viewToAttach, true);
                Animation slideInBottom = AnimationUtils.loadAnimation(ApplicationLoader.getInstance(), R.anim.slide_in_bottom);
                viewToAttach.startAnimation(slideInBottom);
            }

        });

    }

    public static void attachViewToNestedScrollViewState(NestedScrollView nestedScrollView, final View viewToAttach, final View relativeView) {
        nestedScrollView.setOnScrollChangeListener(new NestedViewHideShowScrollListener(viewToAttach) {
            @Override
            public void onHide() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (relativeView.getVisibility() == View.GONE) {
                            runHideAnimation(viewToAttach);
                        }
                    }
                }, 100);
            }

            @Override
            public void onShow() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (relativeView.getVisibility() == View.GONE) {
                            showView(viewToAttach, true);
                            Animation slideInBottom = AnimationUtils.loadAnimation(ApplicationLoader.getInstance(), R.anim.slide_in_bottom);
                            viewToAttach.startAnimation(slideInBottom);
                        }
                    }
                }, 100);

            }

        });

    }

    private static void runHideAnimation(final View viewToAttach) {
        //Hide View
        //Hide the Bottom Space NavigationView here
        Animation slideOutAnimation = AnimationUtils.loadAnimation(ApplicationLoader.getInstance(), R.anim.slide_out_bottom);
        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showView(viewToAttach, false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });
        viewToAttach.startAnimation(slideOutAnimation);
    }

    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static Bundle captureValues(Context c, View view) {
        Bundle b = new Bundle();
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        b.putInt(c.getResources().getString(R.string.view_location_left), screenLocation[0]);
        b.putInt(c.getResources().getString(R.string.view_location_top), screenLocation[1]);
        b.putInt(c.getResources().getString(R.string.view_width), view.getWidth());
        b.putInt(c.getResources().getString(R.string.view_height), view.getHeight());
        return b;
    }

    public static void loadName(ImageView imageView, String name) {
        String notShownMembers = WordUtils.initials(name.toUpperCase());
        int color = generator.getRandomColor();
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .round();
        TextDrawable colouredDrawable = builder.build(notShownMembers, color);
        Bitmap textBitmap = UiUtils.convertDrawableToBitmap(colouredDrawable);
        imageView.setImageBitmap(textBitmap);
    }

    public static void loadNameAsImage(ImageView imageView, String name) {
        int color = generator.getRandomColor();
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .round();
        TextDrawable colouredDrawable = builder.build(name, color);
        Bitmap textBitmap = UiUtils.convertDrawableToBitmap(colouredDrawable);
        imageView.setImageBitmap(textBitmap);
    }

    private static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 80;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 80;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(300);
        v.startAnimation(anim);
    }

    private static int[] otherRandomColors = new int[]{
            R.color.gplus_color_3,
            R.color.teal_900,
            R.color.hollout_color_five,
            R.color.purple_A700,
            R.color.colorFacebook,
            R.color.colorGoogle,
            R.color.colorTwitter,
            R.color.linked_in,
            R.color.text_dark,
            R.color.hollout_material_grey_800,
            R.color.textsecure_primary_dark,
            android.R.color.white
    };

    private static int currentIndex = 0;

    public static int getRandomColor() {
        if (currentIndex > otherRandomColors.length - 1) {
            currentIndex = 0;
            return otherRandomColors[currentIndex];
        }
        int newColor = otherRandomColors[currentIndex];
        currentIndex = currentIndex + 1;
        return newColor;
    }

}
