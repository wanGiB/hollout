package com.wan.hollout.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;
import com.wan.hollout.utils.ViewUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Wan Clem
 ***/
public class InputPanel extends LinearLayout implements KeyboardAwareLinearLayout.OnKeyboardShownListener {

    private static final int FADE_TIME = 150;

    private EmojiToggle emojiToggle;
    private ComposeText composeText;

    private RecordTime recordTime;
    private ImageView recordingStartedAnimationTimer;
    private AnimatingToggle composeOrRecordAnimationToggle;
    private LinearLayout recordingContainer;
    private LinearLayout composeContainer;

    private Animation mFadeInFadeOutAnimation;
    private FloatingActionButton recordButton;
    private AnimatingToggle controlsToggle;

    @Nullable
    private Listener listener;

    public InputPanel(Context context) {
        super(context);
    }

    public InputPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InputPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean canRecord() {
        return controlsToggle.getCurrent().getId() == recordButton.getId();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        this.emojiToggle = ViewUtil.findById(this, R.id.emoji_toggle);
        this.composeText = ViewUtil.findById(this, R.id.embedded_text_editor);

        this.recordTime = new RecordTime((TextView) ViewUtil.findById(this, R.id.record_time));
        TextView slideToCancel = ViewUtil.findById(this, R.id.slide_to_cancel);
        composeOrRecordAnimationToggle = ViewUtil.findById(this, R.id.compose_or_record_toggle);
        recordingContainer = ViewUtil.findById(this, R.id.recording_container);
        composeContainer = ViewUtil.findById(this, R.id.compose_bubble);
        controlsToggle = ViewUtil.findById(this, R.id.control_toggle);
        recordButton = ViewUtil.findById(this, R.id.record_message_button);

        recordingStartedAnimationTimer = ViewUtil.findById(this, R.id.recording_started_animating_image);

        if (HolloutPreferences.isSystemEmojiPreferred()) {
            emojiToggle.setVisibility(View.GONE);
        } else {
            emojiToggle.setVisibility(View.VISIBLE);
        }

        mFadeInFadeOutAnimation = UiUtils.getAnimation(getContext(), android.R.anim.fade_in);
        mFadeInFadeOutAnimation.setRepeatMode(Animation.REVERSE);
        mFadeInFadeOutAnimation.setRepeatCount(Animation.INFINITE);
        mFadeInFadeOutAnimation.setDuration(200);

        slideToCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                cancelRecording();
            }

        });

    }

    public void setListener(final @NonNull Listener listener) {
        this.listener = listener;
        emojiToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEmojiToggle();
            }
        });
    }

    public void startRecorder() {
        if (listener != null) {
            listener.onRecorderStarted();
            composeOrRecordAnimationToggle.display(recordingContainer);
            recordTime.display();
            recordingStartedAnimationTimer.startAnimation(mFadeInFadeOutAnimation);
        }
    }

    public void stopRecording() {
        if (listener != null) {
            recordingStartedAnimationTimer.clearAnimation();
            recordTime.hide();
            composeOrRecordAnimationToggle.display(composeContainer);
            listener.onRecorderFinished();
        }
    }

    private void cancelRecording() {
        recordingStartedAnimationTimer.clearAnimation();
        stopRecording();
    }

    public void onPause() {
        //Pause recording
    }

    public void setEnabled(boolean enabled) {
        composeText.setEnabled(enabled);
        emojiToggle.setEnabled(enabled);
    }

    public long getElapsedTime() {
        return recordTime.getElapsedTime();
    }

    @Override
    public void onKeyboardShown() {
        emojiToggle.setToEmoji();
    }

    public void displayControl(View control) {
        controlsToggle.display(control);
    }

    public void setEmojiToggleToKeyboard() {
        emojiToggle.setToIme();
    }

    public void setEmojiToggleToEmoji() {
        emojiToggle.setToEmoji();
    }

    public interface Listener {
        void onRecorderStarted();

        void onRecorderFinished();

        void onEmojiToggle();
    }

    private static class RecordTime implements Runnable {

        private final TextView recordTimeView;

        private final AtomicLong startTime = new AtomicLong(0);
        private long elapsedTime;
        private final Handler handler = new Handler();

        private RecordTime(TextView recordTimeView) {
            this.recordTimeView = recordTimeView;
        }

        public void display() {
            this.startTime.set(System.currentTimeMillis());
            this.recordTimeView.setText(DateUtils.formatElapsedTime(0));
            ViewUtil.fadeIn(this.recordTimeView, FADE_TIME);
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
        }

        void hide() {
            elapsedTime = System.currentTimeMillis() - startTime.get();
            this.startTime.set(0);
            ViewUtil.fadeOut(this.recordTimeView, FADE_TIME, View.INVISIBLE);
        }

        @Override
        public void run() {
            long localStartTime = startTime.get();
            if (localStartTime > 0) {
                elapsedTime = System.currentTimeMillis() - localStartTime;
                recordTimeView.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(elapsedTime)));
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
            }
        }

        long getElapsedTime() {
            return elapsedTime;
        }

    }

}
