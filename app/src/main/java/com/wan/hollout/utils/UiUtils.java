package com.wan.hollout.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
import com.parse.ParseUser;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.SlidePagerActivity;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.ui.adapters.CircularAdditionalPhotosAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.indexOfIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Wan Clem
 */

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

    public static void dismissKeyboard(View trigger) {
        InputMethodManager imm = (InputMethodManager) ApplicationLoader.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(trigger.getWindowToken(), 0);
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

        String signedInUserStatusVisibility = ParseUser.getCurrentUser().getString(AppConstants.STATUS_VISIBILITY_PREF);
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

    public static boolean canShowLocation(ParseObject parseUser, int entityType, HashMap<String, Object> optionalProps) {

        String signedInUserLocationVisibility = ParseUser.getCurrentUser().getString(AppConstants.LOCATION_VISIBILITY_PREF);
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

    public static boolean canShowPresence(ParseObject parseUser, int type, HashMap<String, Object> optionalProps) {

        String signedInUserLastSeenVisibility = ParseUser.getCurrentUser().getString(AppConstants.LAST_SEEN_VISIBILITY_PREF);
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

    public static boolean canShowAge(ParseObject parseUser, int type, HashMap<String, Object> optionalProps) {
        String signedInUserAgeVisibility = ParseUser.getCurrentUser().getString(AppConstants.AGE_VISIBILITY_PREF);
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

    public static void loadUserData(final Activity activity, final ParseUser parseUser) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ParseUser signedInUser = ParseUser.getCurrentUser();
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(activity);
                @SuppressLint("InflateParams")
                final View profilePreview = activity.getLayoutInflater().inflate(R.layout.preview_profile, null);
                final RecyclerView additionalPhotosRecyclerView = ButterKnife.findById(profilePreview, R.id.additional_photos_recycler_view);
                final RoundedImageView photoView = ButterKnife.findById(profilePreview, R.id.user_cover_photo_view);
                final HolloutTextView onlineStatusView = ButterKnife.findById(profilePreview, R.id.user_online_status);
                final LinearLayout startChatView = ButterKnife.findById(profilePreview, R.id.start_chat);
                final HolloutTextView viewProfileView = ButterKnife.findById(profilePreview, R.id.view_user_profile);
                final HolloutTextView usernameView = ButterKnife.findById(profilePreview, R.id.user_name);
                refreshUserData(signedInUser, additionalPhotosRecyclerView, photoView, onlineStatusView, startChatView, viewProfileView, usernameView, parseUser, activity);
                final ParseQuery<ParseUser> userStateQuery = ParseUser.getQuery();
                userStateQuery.whereEqualTo("objectId", parseUser.getObjectId());
                SubscriptionHandling<ParseUser> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(userStateQuery);
                subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseUser>() {
                    @Override
                    public void onEvent(ParseQuery<ParseUser> query, final ParseUser object) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshUserData(signedInUser, additionalPhotosRecyclerView, photoView, onlineStatusView, startChatView, viewProfileView, usernameView, object, activity);
                            }
                        });
                    }
                });
                sweetAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        try {
                            ApplicationLoader.getParseLiveQueryClient().unsubscribe(userStateQuery);
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

    private static void refreshUserData(final ParseUser signedInUser, RecyclerView additionalPhotosRecyclerView, RoundedImageView photoView, HolloutTextView onlineStatusView, final LinearLayout startChatView, final HolloutTextView viewProfileView, HolloutTextView usernameView, final ParseUser parseUser, final Activity activity) {
        final String username = parseUser.getUsername();
        final String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        Long userLastSeenAt = parseUser.getLong(AppConstants.APP_USER_LAST_SEEN);
        if (HolloutUtils.isNetWorkConnected(ApplicationLoader.getInstance())
                && parseUser.getString(AppConstants.APP_USER_ONLINE_STATUS).
                equals(AppConstants.ONLINE)) {
            attachDrawableToTextView(ApplicationLoader.getInstance(), onlineStatusView, R.drawable.ic_online, DrawableDirection.LEFT);
            onlineStatusView.setText(activity.getString(R.string.online));
        } else {
            removeAllDrawablesFromTextView(onlineStatusView);
            onlineStatusView.setText(getLastSeen(userLastSeenAt));
        }
        if (StringUtils.isNotEmpty(username)) {
            usernameView.setText(StringUtils.capitalize(username));
        }
        final List<String> userPhotos = new ArrayList<>();
        if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
            if (!userPhotos.contains(userProfilePhotoUrl)) {
                userPhotos.add(userProfilePhotoUrl);
            }
            loadImage(activity, userProfilePhotoUrl, photoView);
        }

        List<String> userAdditionalPhotos = parseUser.getList(AppConstants.APP_USER_ADDITIONAL_USER_PHOTOS);
        if (userAdditionalPhotos != null) {
            if (!userPhotos.containsAll(userAdditionalPhotos)) {
                userPhotos.addAll(userAdditionalPhotos);
            }
        }

        CircularAdditionalPhotosAdapter circularAdditionalPhotosAdapter = new
                CircularAdditionalPhotosAdapter(activity, userPhotos, parseUser.getUsername());

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        additionalPhotosRecyclerView.setLayoutManager(horizontalLayoutManager);
        additionalPhotosRecyclerView.setAdapter(circularAdditionalPhotosAdapter);

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
                            mChatIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
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

}
