package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.StoryBox;
import com.wan.hollout.utils.RandomColor;
import com.wan.hollout.utils.UiUtils;
import com.wonderkiln.camerakit.CameraView;

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
    View rootView;

    @BindView(R.id.story_box)
    StoryBox storyBox;

    @BindView(R.id.camera)
    CameraView cameraView;

    private EmojiPopup emojiPopup;
    private RandomColor randomColor;

    private Random random;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);
        ButterKnife.bind(this);
        setupEmojiPopup();
        randomColor = new RandomColor();
        random = new Random();
        useCameraIconView.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera)
                .sizeDp(16).color(Color.WHITE));
        openEmojiView.setOnClickListener(this);
        changeStoryBoardColorView.setOnClickListener(this);
        changeTypefaceView.setOnClickListener(this);
        cameraContainerBackgroundView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
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

        storyBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
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
