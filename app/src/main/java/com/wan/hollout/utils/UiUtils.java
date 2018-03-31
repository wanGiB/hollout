package com.wan.hollout.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.emoji.concurrent.ListenableFuture;
import com.wan.hollout.emoji.concurrent.SettableFuture;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.SlidePagerActivity;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.ui.adapters.FeaturedPhotosCircleAdapter;
import com.wan.hollout.ui.utils.Preconditions;
import com.wan.hollout.ui.widgets.CircularProgressButton;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;
import com.wan.hollout.ui.widgets.SweetAlertDialog;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;

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
                if (context != null) {
                    if (Build.VERSION.SDK_INT >= 17) {
                        if (!context.isDestroyed()) {
                            Glide.with(context).load(photoPath).listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    imageView.setImageResource(R.mipmap.ic_launcher);
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

    public static void loadUserData(final Activity activity, final ParseObject parseUser) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ParseObject signedInUser = AuthUtil.getCurrentUser();
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(activity);
                @SuppressLint("InflateParams") final View profilePreview = activity.getLayoutInflater().inflate(R.layout.preview_profile, null);
                final RecyclerView additionalPhotosRecyclerView = ButterKnife.findById(profilePreview, R.id.additional_photos_recycler_view);
                final RoundedImageView photoView = ButterKnife.findById(profilePreview, R.id.user_cover_photo_view);
                final HolloutTextView onlineStatusView = ButterKnife.findById(profilePreview, R.id.user_online_status);
                final LinearLayout startChatView = ButterKnife.findById(profilePreview, R.id.start_chat);
                final HolloutTextView viewProfileView = ButterKnife.findById(profilePreview, R.id.view_user_profile);
                final HolloutTextView usernameView = ButterKnife.findById(profilePreview, R.id.user_name);
                refreshUserData(signedInUser, additionalPhotosRecyclerView, photoView, onlineStatusView, startChatView, viewProfileView, usernameView, parseUser, activity);
                final ParseQuery<ParseObject> userStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
                userStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, parseUser.getString(AppConstants.REAL_OBJECT_ID));
                try {
                    SubscriptionHandling<ParseObject> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(userStateQuery);
                    subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                        @Override
                        public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshUserData(signedInUser, additionalPhotosRecyclerView, photoView, onlineStatusView, startChatView, viewProfileView, usernameView, object, activity);
                                }
                            });
                        }
                    });
                } catch (NullPointerException ignored) {

                }
                sweetAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        try {
                            ApplicationLoader.getParseLiveQueryClient().unsubscribe(userStateQuery);
                            dialogInterface.cancel();
                        } catch (NullPointerException ignored) {

                        }
                    }
                });
                sweetAlertDialog.setContentView(profilePreview);
                if (!sweetAlertDialog.isShowing()) {
                    sweetAlertDialog.show();
                }
            }
        });
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
        imageView.setColorFilter(color);
    }

    public static void showKeyboard(View trigger) {
        InputMethodManager imm = (InputMethodManager) ApplicationLoader.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(trigger, InputMethodManager.SHOW_FORCED);
    }

    private static void refreshUserData(final ParseObject signedInUser, RecyclerView additionalPhotosRecyclerView, RoundedImageView photoView, HolloutTextView onlineStatusView, final LinearLayout startChatView, final HolloutTextView viewProfileView, HolloutTextView usernameView, final ParseObject parseUser, final Activity activity) {
        final String username = parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
        final String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        Long userLastSeenAt = parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) != 0
                ? parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) :
                parseUser.getLong(AppConstants.APP_USER_LAST_SEEN);
        if (HolloutUtils.isNetWorkConnected(ApplicationLoader.getInstance())
                && parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) == signedInUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP)) {
            attachDrawableToTextView(ApplicationLoader.getInstance(), onlineStatusView, R.drawable.ic_online, DrawableDirection.LEFT);
            onlineStatusView.setText(activity.getString(R.string.online));
        } else {
            removeAllDrawablesFromTextView(onlineStatusView);
            onlineStatusView.setText(getLastSeen(userLastSeenAt));
        }
        if (StringUtils.isNotEmpty(username)) {
            usernameView.setText(WordUtils.capitalize(username));
        }
        final List<String> userPhotos = new ArrayList<>();
        if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
            if (!userPhotos.contains(userProfilePhotoUrl)) {
                userPhotos.add(userProfilePhotoUrl);
            }
            loadImage(activity, userProfilePhotoUrl, photoView);
        }

        List<String> userAdditionalPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
        if (userAdditionalPhotos != null) {
            if (!userPhotos.containsAll(userAdditionalPhotos)) {
                userPhotos.addAll(userAdditionalPhotos);
            }
        }

        FeaturedPhotosCircleAdapter featuredPhotosCircleAdapter = new
                FeaturedPhotosCircleAdapter(activity, userPhotos, parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        additionalPhotosRecyclerView.setLayoutManager(horizontalLayoutManager);
        additionalPhotosRecyclerView.setAdapter(featuredPhotosCircleAdapter);

        tintImageView(photoView, ContextCompat.getColor(activity, R.color.image_tint));

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.start_chat:
                        blinkView(startChatView);
                        String signedInUserProfilePhoto = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                        if (StringUtils.isNotEmpty(signedInUserProfilePhoto)) {
                            Intent mChatIntent = new Intent(activity, ChatActivity.class);
                            parseUser.put(AppConstants.CHAT_TYPE, AppConstants.CHAT_TYPE_SINGLE);
                            mChatIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                            mChatIntent.putExtra(AppConstants.USER_FRIENDABLE, true);
                            activity.startActivity(mChatIntent);
                        } else {
                            Snackbar.make(activity.getWindow().getDecorView(),
                                    R.string.upload_new_photo_first, Snackbar.LENGTH_INDEFINITE).setAction(R.string.UPLOAD,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            HolloutUtils.startImagePicker(activity);
                                        }
                                    }).show();
                        }
                        break;
                    case R.id.view_user_profile:
                        blinkView(viewProfileView);
                        Intent userProfileIntent = new Intent(activity, UserProfileActivity.class);
                        userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                        activity.startActivity(userProfileIntent);
                        break;
                    case R.id.user_cover_photo_view:
                        Intent mProfilePhotoViewIntent = new Intent(activity, SlidePagerActivity.class);
                        mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_TITLE, username);
                        ArrayList<String> photos = HolloutUtils.getAllOfAUserPhotos(userProfilePhotoUrl, userPhotos);
                        mProfilePhotoViewIntent.putStringArrayListExtra(AppConstants.EXTRA_PICTURES, photos);
                        activity.startActivity(mProfilePhotoViewIntent);
                        break;
                }
            }
        };

        startChatView.setOnClickListener(onClickListener);
        viewProfileView.setOnClickListener(onClickListener);
        photoView.setOnClickListener(onClickListener);
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

    public static void loadMusicPreview(Activity activity, ImageView audioIcon, Uri uri) {
        if (Build.VERSION.SDK_INT >= 17) {
            if (!activity.isDestroyed()) {
                Glide.with(activity).load(uri).error(R.drawable.x_ic_folde_music).placeholder(R.drawable.x_ic_folde_music).crossFade().into(audioIcon);
                audioIcon.invalidate();
            }
        } else {
            Glide.with(activity).load(uri).error(R.drawable.x_ic_folde_music).placeholder(R.drawable.x_ic_folde_music).crossFade().into(audioIcon);
            audioIcon.invalidate();
        }
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

    private Object workLifeBalance;

    private void makeEveryMomentCount() {
        if (workLifeBalance == null) {
            pauseWork();
            forgetAboutAnythingThatMightGoWrong();
            try {
                catchLoadsAndLoadsOfFun();
            } catch (FunException e) {
                handleExceptionAndContinueFun();
            }
        }
    }

    private void handleExceptionAndContinueFun() {

    }

    static class FunException extends Exception {

    }

    private void catchLoadsAndLoadsOfFun() throws FunException {

    }

    private void forgetAboutAnythingThatMightGoWrong() {

    }

    private void pauseWork() {

    }

}
