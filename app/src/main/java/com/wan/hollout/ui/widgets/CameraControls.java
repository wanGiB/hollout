package com.wan.hollout.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.SessionType;
import com.wan.hollout.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class CameraControls extends LinearLayout {

    private int cameraViewId = -1;
    private CameraView cameraView;

    private int coverViewId = -1;
    private View coverView;

    @BindView(R.id.facingButton)
    ImageView facingButton;

    @BindView(R.id.flashButton)
    ImageView flashButton;

    @BindView(R.id.captureButton)
    ImageView captureButton;

    private long captureDownTime;
    private long captureStartTime;
    private boolean pendingVideoCapture;
    private boolean capturingVideo;

    public interface CameraStateChangeListener {

        void photoCaptureStared(long captureStartTime);

        void setVideoCaptureStopped();

        void setVideoCaptureStarted(long captureDownTime);

        void onPictureCaptured(byte[] bytesArray);
    }

    private CameraStateChangeListener cameraStateChangeListener;

    public CameraControls(Context context) {
        this(context, null);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.camera_controls, this);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CameraControls,
                    0, 0);

            try {
                cameraViewId = a.getResourceId(R.styleable.CameraControls_camera, -1);
                coverViewId = a.getResourceId(R.styleable.CameraControls_cover, -1);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (cameraViewId != -1) {
            View view = getRootView().findViewById(cameraViewId);
            if (view instanceof CameraView) {
                cameraView = (CameraView) view;
                cameraView.setVideoMaxSize(3600000);
                cameraView.setPlaySounds(true);
                cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
                cameraView.addCameraListener(new CameraListener() {

                    @Override
                    public void onCameraOpened(CameraOptions options) {
                        super.onCameraOpened(options);
                    }

                    @Override
                    public void onCameraClosed() {
                        super.onCameraClosed();
                    }

                    @Override
                    public void onPictureTaken(byte[] jpeg) {
                        super.onPictureTaken(jpeg);
                        cameraStateChangeListener.onPictureCaptured(jpeg);
                    }

                    @Override
                    public void onVideoTaken(File video) {
                        super.onVideoTaken(video);
                    }

                });

                setFacingImageBasedOnCamera();

            }

        }

        if (coverViewId != -1) {
            View view = getRootView().findViewById(coverViewId);
            if (view != null) {
                coverView = view;
                coverView.setVisibility(GONE);
            }
        }

        captureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingVideoCapture = false;
                if (capturingVideo) {
                    capturingVideo = false;
                    cameraView.stopCapturingVideo();
                    cameraStateChangeListener.setVideoCaptureStopped();
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.capture_button);
                    captureButton.setImageDrawable(drawable);
                    cameraView.setSessionType(SessionType.PICTURE);
                } else {
                    captureStartTime = System.currentTimeMillis();
                    cameraView.capturePicture();
                    cameraStateChangeListener.photoCaptureStared(captureStartTime);
                }
            }
        });

        captureButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                captureDownTime = System.currentTimeMillis();
                pendingVideoCapture = true;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pendingVideoCapture) {
                            capturingVideo = true;
                            cameraView.setSessionType(SessionType.VIDEO);
                            cameraView.startCapturingVideo(null);
                            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.video_capture_started);
                            captureButton.setImageDrawable(drawable);
                            cameraStateChangeListener.setVideoCaptureStarted(captureDownTime);
                        }
                    }
                }, 250);
                return false;
            }
        });
    }

    private void setFacingImageBasedOnCamera() {
        if (cameraView.getFacing() == Facing.FRONT) {
            facingButton.setImageResource(R.drawable.ic_facing_back);
        } else {
            facingButton.setImageResource(R.drawable.ic_facing_front);
        }
    }

    @OnTouch(R.id.facingButton)
    boolean onTouchFacing(final View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                coverView.setAlpha(0);
                coverView.setVisibility(VISIBLE);
                coverView.animate()
                        .alpha(1)
                        .setStartDelay(0)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (cameraView.getFacing() == Facing.FRONT) {
                                    cameraView.setFacing(Facing.BACK);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_front);
                                } else {
                                    cameraView.setFacing(Facing.FRONT);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_back);
                                }
                                coverView.animate()
                                        .alpha(0)
                                        .setStartDelay(200)
                                        .setDuration(300)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                coverView.setVisibility(GONE);
                                            }
                                        })
                                        .start();
                            }
                        })
                        .start();

                break;
            }
        }
        return true;
    }

    @OnTouch(R.id.flashButton)
    boolean onTouchFlash(View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                if (cameraView.getFlash() == Flash.OFF) {
                    cameraView.setFlash(Flash.ON);
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_on);
                } else {
                    cameraView.setFlash(Flash.ON);
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_off);
                }
                break;
            }
        }
        return true;
    }

    void handleViewTouchFeedback(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchDownAnimation(view);
                break;
            }
            case MotionEvent.ACTION_UP: {
                touchUpAnimation(view);
                break;
            }
        }
    }

    void touchDownAnimation(View view) {
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void changeViewImageResource(final ImageView imageView, @DrawableRes final int resId) {
        imageView.setRotation(0);
        imageView.animate()
                .rotationBy(360)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();

        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(resId);
            }
        }, 120);
    }

    public void setOnCameraStateChangeListener(CameraStateChangeListener cameraStateChangeListener) {
        this.cameraStateChangeListener = cameraStateChangeListener;
    }

}
