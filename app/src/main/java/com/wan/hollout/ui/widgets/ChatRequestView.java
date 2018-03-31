package com.wan.hollout.ui.widgets;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.ChatRequestNegotiationResult;
import com.wan.hollout.listeners.SwipeGestureListener;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"RedundantCast", "ConstantConditions"})
public class ChatRequestView extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

    private Activity activity;

    static final String TAG = "ChatRequestSwipeActionView";
    static final int ANIMATE_SWIPE_CLOSE_DURATION = 500;

    private View swipeContainer;

    private View rootView;
    private HolloutTextView requesterNameView;
    private ImageView requesterPhotoView;
    private View declineContainer;
    private View acceptContainer;
    private View playDivider;
    private HolloutTextView aboutRequesterView;
    private HolloutTextView distanceToRequesterView;

    private ParseObject requestOriginator;

    protected float minimumXtoHandleEvents;
    protected boolean gestureOnSwipeDetected;
    protected boolean gestureOnScrollDetected;
    protected GestureDetector gestureDetector;
    protected OnSwipedListener onSwipedListener;

    public interface OnSwipedListener {
        void onSwipeLeft();

        void onSwipeRight();
    }

    public ChatRequestView(Context context) {
        this(context, null);
    }

    public ChatRequestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRequestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.chat_request_item, this);
        init();
    }

    public void bindData(final Activity activity, final ParseObject feedObject, final int position) {
        this.activity = activity;
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        UiUtils.generateRandomBackgroundColor(playDivider, position);
        if (feedObject != null) {
            String requestType = feedObject.getString(AppConstants.FEED_TYPE);
            if (requestType.equals(AppConstants.FEED_TYPE_CHAT_REQUEST)) {
                requestOriginator = feedObject.getParseObject(AppConstants.FEED_CREATOR);
                if (requestOriginator != null) {
                    final String userDisplayName = requestOriginator.getString(AppConstants.APP_USER_DISPLAY_NAME);
                    requesterNameView.setText(WordUtils.capitalize(userDisplayName));
                    String userProfilePhotoUrl = requestOriginator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                    if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                        UiUtils.loadImage(activity, userProfilePhotoUrl, requesterPhotoView);
                    } else {
                        requesterPhotoView.setImageResource(R.drawable.empty_profile);
                    }
                    List<String> aboutUser = requestOriginator.getList(AppConstants.ABOUT_USER);
                    List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);
                    if (aboutUser != null && aboutSignedInUser != null) {
                        try {
                            List<String> common = new ArrayList<>(aboutUser);
                            common.retainAll(aboutSignedInUser);
                            String firstInterest = !common.isEmpty() ? common.get(0) : aboutUser.get(0);
                            aboutRequesterView.setText(WordUtils.capitalize(firstInterest));
                        } catch (NullPointerException ignored) {

                        }
                    }
                    ParseGeoPoint userGeoPoint = requestOriginator.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
                    ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
                    if (signedInUserGeoPoint != null && userGeoPoint != null) {
                        double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                        String value = HolloutUtils.formatDistance(distanceInKills);
                        String formattedDistanceToUser = HolloutUtils.formatDistanceToUser(value);
                        if (formattedDistanceToUser != null) {
                            UiUtils.showView(distanceToRequesterView, true);
                            distanceToRequesterView.setText(formattedDistanceToUser.concat("KM from you"));
                        } else {
                            UiUtils.showView(distanceToRequesterView, false);
                        }
                    } else {
                        UiUtils.setTextOnView(distanceToRequesterView, " ");
                    }

                }
            }
            setOnSwipedListener(new OnSwipedListener() {

                @Override
                public void onSwipeLeft() {
                    EventBus.getDefault().post(new ChatRequestNegotiationResult(feedObject, true, position));
                    HolloutUtils.acceptOrDeclineChat(feedObject, true, requestOriginator.getString(AppConstants.REAL_OBJECT_ID),
                            requestOriginator.getString(AppConstants.APP_USER_DISPLAY_NAME));
                }

                @Override
                public void onSwipeRight() {
                    EventBus.getDefault().post(new ChatRequestNegotiationResult(feedObject, true, position));
                    HolloutUtils.acceptOrDeclineChat(feedObject, false, requestOriginator.getString(AppConstants.REAL_OBJECT_ID),
                            requestOriginator.getString(AppConstants.APP_USER_DISPLAY_NAME));

                }
            });
        }
    }

    private void init() {
        initViews();
        rootView.setOnClickListener(this);
        rootView.setOnLongClickListener(this);
    }

    private void initViews() {
        minimumXtoHandleEvents = getResources().getDimension(R.dimen.dimen_swipe_minumum_x);
        playDivider = findViewById(R.id.play_divider);
        swipeContainer = findViewById(R.id.requester_bio_data_container);
        rootView = findViewById(R.id.rootLayout);
        requesterPhotoView = findViewById(R.id.requester_profile_photo);
        requesterNameView = (HolloutTextView) findViewById(R.id.requester_name);
        aboutRequesterView = (HolloutTextView) findViewById(R.id.about_requester);
        distanceToRequesterView = (HolloutTextView) findViewById(R.id.distance_to_requester);
        acceptContainer = findViewById(R.id.accept_button);
        declineContainer = findViewById(R.id.decline_button);
        initGestureDetector();
    }

    @Override
    public void onClick(View v) {
        UiUtils.blinkView(v);
        if (requestOriginator != null) {
            Intent requesterInfoIntent = new Intent(activity, UserProfileActivity.class);
            requesterInfoIntent.putExtra(AppConstants.USER_PROPERTIES, requestOriginator);
            activity.startActivity(requesterInfoIntent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener(SwipeGestureListener.Sensitivity.LOW) {

            private boolean animShown = false;
            private boolean animRunning = false;

            @Override
            protected boolean onSwipeLeft(float velocityX) {
                gestureOnSwipeDetected = true;
                notifySwipedLeft();
                return true;
            }

            @Override
            protected boolean onSwipeRight(float velocityX) {
                gestureOnSwipeDetected = true;
                notifySwipedRight();
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                // New touch - reset variables
                gestureOnSwipeDetected = false;
                gestureOnScrollDetected = false;
                animShown = false;
                animRunning = false;
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // Block scroll up/down of list while item is being scrolled right/left
                requestDisallowInterceptTouchEvent(true);

                int leftMargin = (int) swipeContainer.getX();

                // Allow movement only up to 1/3 of the screen
                int maxMovement = UiUtils.getScreenWidth();
                if (Math.abs(leftMargin - distanceX) < maxMovement) {
                    swipeContainer.setX(leftMargin - distanceX);
                } else if (leftMargin != maxMovement) {
                    //first time we are passing the max movement barier - setting the X position to be exactly as max movement
                    if (leftMargin < 0) swipeContainer.setX(-maxMovement);
                    else swipeContainer.setX(maxMovement);
                }
                // Handle showing of Accept/Decline icons animation
                if (!animShown && !animRunning) {
                    if (Math.abs(leftMargin) > UiUtils.getScreenWidth() / 6) {
                        Animation heartBeatAnim = AnimationUtils.loadAnimation(getContext(), R.anim.heart_beat);
                        heartBeatAnim.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                animRunning = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }

                        });
                        View icon = leftMargin < 0 ? acceptContainer : declineContainer;
                        UiUtils.showView(icon, true);
                        icon.startAnimation(heartBeatAnim);
                        if (icon.getId() == acceptContainer.getId()) {
                            UiUtils.showView(declineContainer, false);
                        } else {
                            UiUtils.showView(acceptContainer, false);
                        }
                        animShown = true;
                        animRunning = true;
                    }
                }

                // User passed back the animation threshold - reset
                if (Math.abs(leftMargin) < UiUtils.getScreenWidth() / 6) {
                    animShown = false;
                }

                gestureOnScrollDetected = true;

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Only handle touch down events that are not on the SlidingMenu's swipe margin
        // Allow handling of other events (such as up) to allow animating item closing and etc.
        if ((ev.getAction() != MotionEvent.ACTION_UP) && (ev.getRawX() <= minimumXtoHandleEvents)) {
            return false;
        }

        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                if (gestureOnScrollDetected) {
                    animateScroll((int) swipeContainer.getX());
                    return true;
                }
            }

            if (gestureOnScrollDetected) {
                return true;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private void animateScroll(int from) {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(swipeContainer, View.TRANSLATION_X, from, 0);
        animator1.setDuration(ANIMATE_SWIPE_CLOSE_DURATION);
        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator1.setInterpolator(new OvershootInterpolator(2f));
        animator1.start();
    }

    public void setOnSwipedListener(OnSwipedListener l) {
        this.onSwipedListener = l;
    }

    private void notifySwipedLeft() {
        HolloutLogger.d(TAG, "Notifying swipe left");
        if (onSwipedListener != null) {
            onSwipedListener.onSwipeLeft();
        }
    }

    private void notifySwipedRight() {
        HolloutLogger.d(TAG, "Notifying swipe right");
        if (onSwipedListener != null) {
            onSwipedListener.onSwipeRight();
        }
    }

}