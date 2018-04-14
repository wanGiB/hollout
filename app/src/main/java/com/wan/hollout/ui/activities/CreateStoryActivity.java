package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.otaliastudios.cameraview.CameraView;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.wan.hollout.R;
import com.wan.hollout.listeners.OnSwipeTouchListener;
import com.wan.hollout.ui.widgets.CameraControls;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RecentPhotoViewRail;
import com.wan.hollout.ui.widgets.StoryBox;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.RandomColor;
import com.wan.hollout.utils.UiUtils;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class CreateStoryActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.open_emoji)
    ImageView openEmojiView;

    @BindView(R.id.change_color)
    ImageView changeStoryBoardColorView;

    @BindView(R.id.change_typeface)
    ImageView changeTypefaceView;

    @BindView(R.id.camera_container_background)
    View cameraContainerBackgroundView;

    @BindView(R.id.use_camera_instead_icon)
    ImageView useCameraIconView;

    @BindView(R.id.rootLayout)
    LinearLayout rootView;

    @BindView(R.id.story_box)
    StoryBox storyBox;

    @BindView(R.id.camera)
    CameraView cameraView;

    @BindView(R.id.camera_controls)
    CameraControls cameraControls;

    @BindView(R.id.recent_photos)
    RecentPhotoViewRail recentPhotoViewRail;

    @BindView(R.id.drag_photo_trail_up_icon)
    ImageView dragPhotoTrailUpIcon;

    @BindView(R.id.video_started_timer)
    HolloutTextView videoStartedTimer;

    @BindView(R.id.bottom_bar)
    View bottomBar;

    private EmojiPopup emojiPopup;
    private RandomColor randomColor;

    private Random random;

    private HolloutPermissions holloutPermissions;

    private Vibrator vibrator;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);
        ButterKnife.bind(this);
        setupEmojiPopup();
        randomColor = new RandomColor();
        random = new Random();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        useCameraIconView.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera)
                .sizeDp(16).color(Color.WHITE));

        dragPhotoTrailUpIcon.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_expand_less)
                .sizeDp(18).color(Color.WHITE));

        contentFlipper.setInAnimation(this, R.anim.animation_toggle_in);
        contentFlipper.setOutAnimation(this, R.anim.animation_toggle_out);

        openEmojiView.setOnClickListener(this);
        changeStoryBoardColorView.setOnClickListener(this);
        changeTypefaceView.setOnClickListener(this);
        cameraContainerBackgroundView.setOnClickListener(this);
        holloutPermissions = new HolloutPermissions(this, rootView);
        initCamera();
        loadRecentPhotos();

        dragPhotoTrailUpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bottomBar.setOnTouchListener(new OnSwipeTouchListener(this) {

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                UiUtils.showSafeToast("OnSwipeUpDetected");
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
            }

        });

    }

    public void vibrateVibrator() {
        vibrator.vibrate(100);
    }

    private void loadRecentPhotos() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionsUtils.checkSelfForStoragePermission(this)) {
            holloutPermissions.requestStoragePermissions();
            return;
        }
        getSupportLoaderManager().initLoader(1, null, recentPhotoViewRail);
        recentPhotoViewRail.setListener(new RecentPhotoViewRail.OnItemClickedListener() {
            @Override
            public void onItemClicked(Uri uri) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadRecentPhotos();
    }

    private void initCamera() {
        cameraControls.setOnCameraStateChangeListener(new CameraControls.CameraStateChangeListener() {

            @Override
            public void photoCaptureStared(long captureStartTime) {

            }

            @Override
            public void setVideoCaptureStopped() {

            }

            @Override
            public void setVideoCaptureStarted(long captureDownTime) {
                vibrateVibrator();
                UiUtils.showView(videoStartedTimer, true);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {

                    }
                }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(int keyBoardHeight) {

                    }
                }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {

                    }
                })
                .build(storyBox);
        initStoryBoxTouchListener();
    }

    private void initStoryBoxTouchListener() {
        storyBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
                UiUtils.showKeyboard(storyBox);
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_emoji:
                emojiPopup.toggle();
                break;
            case R.id.change_color:
                randomizeColor();
                break;
            case R.id.change_typeface:
                storyBox.applyCustomFont(this, random.nextInt(12));
                break;
            case R.id.camera_container_background:
                UiUtils.toggleFlipperState(contentFlipper, 1);
                break;
        }
    }

    private void randomizeColor() {
        int randomCol = randomColor.randomColor();
        randomCol = randomColor.randomColor(randomCol, null, RandomColor.Luminosity.DARK);
        rootView.setBackgroundColor(randomCol);
        cameraContainerBackgroundView.setBackgroundColor(UiUtils.darker(randomCol, 0.9f));
        tintToolbarAndTabLayout(randomCol);
    }

    private void tintToolbarAndTabLayout(int colorPrimary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(UiUtils.darker(colorPrimary, 0.9f));
        }
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            return;
        }
        if (contentFlipper.getDisplayedChild() != 0) {
            contentFlipper.setDisplayedChild(0);
            return;
        }
        super.onBackPressed();
    }

}
