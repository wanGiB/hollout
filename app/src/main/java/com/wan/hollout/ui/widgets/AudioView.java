package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.eventbuses.PartProgressEvent;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioView extends FrameLayout implements AudioSlidePlayer.Listener {

    private static final String TAG = AudioView.class.getSimpleName();

    private final
    @NonNull
    AnimatingToggle controlToggle;
    private final
    @NonNull
    ImageView playButton;
    private final
    @NonNull
    ImageView pauseButton;
    private final
    @NonNull
    SeekBar seekBar;
    private final
    @NonNull
    TextView timestamp;

    private
    @Nullable
    AudioSlidePlayer audioSlidePlayer;

    private TextView audioTitleView;
    private int backwardsCounter;

    @Nullable
    private String audioSlide;

    public AudioView(Context context) {
        this(context, null);
    }

    public AudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.audio_view, this);

        this.controlToggle = (AnimatingToggle) findViewById(R.id.control_toggle);
        this.playButton = (ImageView) findViewById(R.id.play);
        this.pauseButton = (ImageView) findViewById(R.id.pause);
        this.seekBar = (SeekBar) findViewById(R.id.seek);
        this.timestamp = (TextView) findViewById(R.id.timestamp);
        this.audioTitleView = (TextView) findViewById(R.id.audio_title);

        this.playButton.setOnClickListener(new PlayClickedListener());
        this.pauseButton.setOnClickListener(new PauseClickedListener());
        this.seekBar.setOnSeekBarChangeListener(new SeekBarModifiedListener());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.playButton.setImageDrawable(context.getDrawable(R.drawable.play_icon));
            this.pauseButton.setImageDrawable(context.getDrawable(R.drawable.pause_icon));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public void setAudio(final @NonNull String audioFilePath, String audioTitle, String audioDuration) {
        this.audioSlide = audioFilePath;
        controlToggle.displayQuick(playButton);
        seekBar.setEnabled(true);
        this.audioSlidePlayer = AudioSlidePlayer.createFor(getContext(), audioFilePath, this);
        this.timestamp.setText(audioDuration);
        this.audioTitleView.setText(audioTitle);
    }

    public void cleanup() {
        if (this.audioSlidePlayer != null && pauseButton.getVisibility() == View.VISIBLE) {
            this.audioSlidePlayer.stop();
        }
    }

    @Override
    public void onStart() {
        if (this.pauseButton.getVisibility() != View.VISIBLE) {
            togglePlayToPause();
        }
    }

    @Override
    public void onStop() {
        if (this.playButton.getVisibility() != View.VISIBLE) {
            togglePauseToPlay();
        }

        if (seekBar.getProgress() + 5 >= seekBar.getMax()) {
            backwardsCounter = 4;
            onProgress(0.0, 0);
        }
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        this.playButton.setFocusable(focusable);
        this.pauseButton.setFocusable(focusable);
        this.seekBar.setFocusable(focusable);
        this.seekBar.setFocusableInTouchMode(focusable);
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        this.playButton.setClickable(clickable);
        this.pauseButton.setClickable(clickable);
        this.seekBar.setClickable(clickable);
        this.seekBar.setOnTouchListener(clickable ? null : new TouchIgnoringListener());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.playButton.setEnabled(enabled);
        this.pauseButton.setEnabled(enabled);
        this.seekBar.setEnabled(enabled);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onProgress(double progress, long millis) {
        int seekProgress = (int) Math.floor(progress * this.seekBar.getMax());

        if (seekProgress > seekBar.getProgress() || backwardsCounter > 3) {
            backwardsCounter = 0;
            this.seekBar.setProgress(seekProgress);
            this.timestamp.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis)));
        } else {
            backwardsCounter++;
        }
    }

    private double getProgress() {
        if (this.seekBar.getProgress() <= 0 || this.seekBar.getMax() <= 0) {
            return 0;
        } else {
            return (double) this.seekBar.getProgress() / (double) this.seekBar.getMax();
        }
    }

    private void togglePlayToPause() {
        controlToggle.displayQuick(pauseButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable playToPauseDrawable = (AnimatedVectorDrawable) getContext().getDrawable(R.drawable.play_to_pause_animation);
            pauseButton.setImageDrawable(playToPauseDrawable);
            if (playToPauseDrawable != null) {
                playToPauseDrawable.start();
            }
        }
    }

    private void togglePauseToPlay() {
        controlToggle.displayQuick(playButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable pauseToPlayDrawable = (AnimatedVectorDrawable) getContext().getDrawable(R.drawable.pause_to_play_animation);
            playButton.setImageDrawable(pauseToPlayDrawable);
            if (pauseToPlayDrawable != null) {
                pauseToPlayDrawable.start();
            }
        }
    }

    private class PlayClickedListener implements OnClickListener {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            try {
                Log.w(TAG, "playbutton onClick");
                if (audioSlidePlayer != null) {
                    togglePlayToPause();
                    if (audioSlide != null) {
                        audioSlidePlayer.play(getProgress(), Uri.parse(audioSlide));
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }

    private class PauseClickedListener implements OnClickListener {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            Log.w(TAG, "pausebutton onClick");
            if (audioSlidePlayer != null) {
                togglePauseToPlay();
                audioSlidePlayer.stop();
            }
        }
    }

    private class SeekBarModifiedListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public synchronized void onStartTrackingTouch(SeekBar seekBar) {
            if (audioSlidePlayer != null && pauseButton.getVisibility() == View.VISIBLE) {
                audioSlidePlayer.stop();
            }
        }

        @Override
        public synchronized void onStopTrackingTouch(SeekBar seekBar) {
            try {
                if (audioSlidePlayer != null && pauseButton.getVisibility() == View.VISIBLE) {
                    if (audioSlide != null) {
                        audioSlidePlayer.play(getProgress(), Uri.parse(audioSlide));
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }

    private class TouchIgnoringListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final PartProgressEvent event) {
        if (audioSlidePlayer != null && event.attachment.equals(this.audioSlidePlayer.getAudioSlide())) {
            UiUtils.runOnMain(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }

}
