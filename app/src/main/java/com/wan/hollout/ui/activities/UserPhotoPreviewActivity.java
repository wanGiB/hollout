package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.adapters.FeaturedPhotosCircleAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.utils.UiUtils.getLastSeen;

/**
 * @author Wan Clem
 */

public class UserPhotoPreviewActivity extends AppCompatActivity {

    @BindView(R.id.background)
    View background;

    @BindView(R.id.additional_photos_recycler_view)
    RecyclerView additionalPhotosRecyclerView;

    @BindView(R.id.user_cover_photo_view)
    RoundedImageView photoView;

    @BindView(R.id.user_online_status)
    HolloutTextView onlineStatusView;

    @BindView(R.id.start_chat)
    LinearLayout startChatView;

    @BindView(R.id.view_user_profile)
    HolloutTextView viewProfileView;

    @BindView(R.id.user_name)
    HolloutTextView usernameView;

    @BindView(R.id.profile_layout)
    View profileLayout;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    Bundle startValues;
    Bundle endValues;
    float scaleX;
    float scaleY;
    int deltaX;
    int deltaY;
    int animationDuration = 200;

    private ParseQuery<ParseObject> userStateQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_photo_preview);
        ButterKnife.bind(this);
        tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            extractViewInfo(getIntent());
            ParseObject parseObject = bundle.getParcelable(AppConstants.EXTRA_USER);
            if (parseObject != null) {
                onUiReady();
                final ParseQuery<ParseObject> userStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
                userStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, parseObject.getString(AppConstants.REAL_OBJECT_ID));
                loadUserData(parseObject);
            }
        }
    }

    private void tintToolbarAndTabLayout(int colorPrimary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(UiUtils.darker(colorPrimary, 0.9f));
        }
    }

    public void loadUserData(final ParseObject parseUser) {
        final ParseObject signedInUser = AuthUtil.getCurrentUser();
        refreshUserData(signedInUser, additionalPhotosRecyclerView, photoView, onlineStatusView, startChatView, viewProfileView, usernameView, parseUser);
        final ParseQuery<ParseObject> userStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        userStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, parseUser.getString(AppConstants.REAL_OBJECT_ID));
        try {
            SubscriptionHandling<ParseObject> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(userStateQuery);
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                @Override
                public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshUserData(signedInUser, additionalPhotosRecyclerView, photoView, onlineStatusView, startChatView, viewProfileView, usernameView, object);
                        }
                    });
                }
            });
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyListeners();
    }

    @Override
    public void onBackPressed() {
        destroyListeners();
        runExitAnimation();
    }

    private void destroyListeners() {
        try {
            if (userStateQuery != null) {
                ApplicationLoader.getParseLiveQueryClient().unsubscribe(userStateQuery);
                userStateQuery.cancel();
                userStateQuery = null;
            }
        } catch (NullPointerException ignored) {

        }
    }

    private void refreshUserData(final ParseObject signedInUser, RecyclerView additionalPhotosRecyclerView, final RoundedImageView photoView, HolloutTextView onlineStatusView, final LinearLayout startChatView, final HolloutTextView viewProfileView, HolloutTextView usernameView, final ParseObject parseUser) {
        final String username = parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
        final String userId = parseUser.getString(AppConstants.REAL_OBJECT_ID);
        final String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        Long userLastSeenAt = parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) != 0
                ? parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) :
                parseUser.getLong(AppConstants.APP_USER_LAST_SEEN);
        if (HolloutUtils.isNetWorkConnected(ApplicationLoader.getInstance())
                && parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) == signedInUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP)) {
            UiUtils.attachDrawableToTextView(ApplicationLoader.getInstance(), onlineStatusView, R.drawable.ic_online, UiUtils.DrawableDirection.LEFT);
            onlineStatusView.setText(getString(R.string.online));
        } else {
            UiUtils.removeAllDrawablesFromTextView(onlineStatusView);
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
            Glide.with(this).load(userProfilePhotoUrl).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    UiUtils.loadName(photoView, username);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    UiUtils.showView(progressBar, false);
                    return false;
                }

            }).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(photoView);
        }

        List<String> userAdditionalPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
        if (userAdditionalPhotos != null) {
            if (!userPhotos.containsAll(userAdditionalPhotos)) {
                userPhotos.addAll(userAdditionalPhotos);
            }
        }

        FeaturedPhotosCircleAdapter featuredPhotosCircleAdapter = new
                FeaturedPhotosCircleAdapter(this, userPhotos,
                parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME),
                parseUser.getString(AppConstants.REAL_OBJECT_ID));

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        additionalPhotosRecyclerView.setLayoutManager(horizontalLayoutManager);
        additionalPhotosRecyclerView.setAdapter(featuredPhotosCircleAdapter);

        UiUtils.tintImageViewNoMode(photoView, ContextCompat.getColor(this, R.color.image_tint));

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.start_chat:
                        UiUtils.blinkView(startChatView);
                        String signedInUserProfilePhoto = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                        if (StringUtils.isNotEmpty(signedInUserProfilePhoto)) {
                            Intent mChatIntent = new Intent(UserPhotoPreviewActivity.this, ChatActivity.class);
                            parseUser.put(AppConstants.CHAT_TYPE, AppConstants.CHAT_TYPE_SINGLE);
                            mChatIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                            mChatIntent.putExtra(AppConstants.USER_FRIENDABLE, true);
                            startActivity(mChatIntent);
                        } else {
                            Snackbar.make(getWindow().getDecorView(),
                                    R.string.upload_new_photo_first, Snackbar.LENGTH_INDEFINITE).setAction(R.string.UPLOAD,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            HolloutUtils.startImagePicker(UserPhotoPreviewActivity.this);
                                        }
                                    }).show();
                        }
                        break;
                    case R.id.view_user_profile:
                        UiUtils.blinkView(viewProfileView);
                        Intent userProfileIntent = new Intent(UserPhotoPreviewActivity.this, UserProfileActivity.class);
                        userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                        startActivity(userProfileIntent);
                        break;
                    case R.id.user_cover_photo_view:
                        Intent mProfilePhotoViewIntent = new Intent(UserPhotoPreviewActivity.this, SlidePagerActivity.class);
                        mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_TITLE, username);
                        ArrayList<String> photos = HolloutUtils.getAllOfAUserPhotos(userProfilePhotoUrl, userPhotos);
                        mProfilePhotoViewIntent.putStringArrayListExtra(AppConstants.EXTRA_PICTURES, photos);
                        mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_USER_ID, userId);
                        startActivity(mProfilePhotoViewIntent);
                        break;
                }
            }
        };

        startChatView.setOnClickListener(onClickListener);
        viewProfileView.setOnClickListener(onClickListener);
        photoView.setOnClickListener(onClickListener);
    }


    private void extractViewInfo(Intent i) {
        startValues = i.getBundleExtra(getString(R.string.view_info));
    }

    private void onUiReady() {
        profileLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                profileLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                prepareScene();
                runEnterAnimation();
                return true;
            }
        });
    }

    private void prepareScene() {
        endValues = UiUtils.captureValues(this, profileLayout);
        if (startValues != null && endValues != null) {
            // calculate the scale and position deltas
            scaleX = scaleDelta(startValues, endValues, getString(R.string.view_width));
            scaleY = scaleDelta(startValues, endValues, getString(R.string.view_height));
            deltaX = translationDelta(startValues, endValues, getString(R.string.view_location_left));
            deltaY = translationDelta(startValues, endValues, getString(R.string.view_location_top));

            //fix the scaling effect
            deltaX = (int) (deltaX - ((profileLayout.getWidth() - (profileLayout.getWidth() * scaleX)) / 2));
            deltaY = (int) (deltaY - ((profileLayout.getHeight() - (profileLayout.getHeight() * scaleY)) / 2));

            // scale and reposition the image
            profileLayout.setScaleX(scaleX);
            profileLayout.setScaleY(scaleY);
            profileLayout.setTranslationX(deltaX);
            profileLayout.setTranslationY(deltaY);
            background.setAlpha(0.0f);
        }
    }

    private void runEnterAnimation() {
        background.setVisibility(View.VISIBLE);
        profileLayout.setVisibility(View.VISIBLE);
        // finally, run the animation
        background.animate()
                .setDuration(animationDuration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(1.0f)
                .start();
        profileLayout.animate()
                .setDuration(animationDuration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0)
                .translationY(0)
                .start();
    }

    private void runExitAnimation() {
        background.animate()
                .setDuration(animationDuration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .alpha(0.0f)
                .start();
        profileLayout.animate()
                .setDuration(animationDuration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .scaleX(scaleX)
                .scaleY(scaleY)
                .translationX(deltaX)
                .translationY(deltaY)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(0, 0);
                    }
                }).start();
    }

    private float scaleDelta(@NonNull Bundle startValues, @NonNull Bundle endValues, @NonNull String propertyName) {
        int startValue = startValues.getInt(propertyName);
        int endValue = endValues.getInt(propertyName);
        return (float) startValue / endValue;
    }

    private int translationDelta(@NonNull Bundle startValues, @NonNull Bundle endValues, @NonNull String propertyName) {
        int startValue = startValues.getInt(propertyName);
        int endValue = endValues.getInt(propertyName);
        return startValue - endValue;
    }

    public void onClickBackground(View v) {
        runExitAnimation();
    }

}
