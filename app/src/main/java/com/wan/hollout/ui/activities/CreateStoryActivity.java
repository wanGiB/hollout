package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.otaliastudios.cameraview.CameraView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.SelectedFileUriEvent;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.ui.adapters.PhotosAndVideosAdapter;
import com.wan.hollout.ui.widgets.CameraControls;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RecentPhotoViewRail;
import com.wan.hollout.ui.widgets.StoryBox;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
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

    @BindView(R.id.dragView)
    View dragView;

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    @BindView(R.id.more_media_recycler_view)
    RecyclerView moreMediaRecyclerView;

    @BindView(R.id.image_preview)
    ImageView imagePreview;

    @BindView(R.id.filters_recycler_view)
    RecyclerView filtersRecyclerView;

    @BindView(R.id.toggle_media)
    Spinner toggleMediaSpinner;

    @SuppressLint("StaticFieldLeak")
    public static FloatingActionButton doneWithContentSelection;

    private EmojiPopup emojiPopup;

    private Random random;
    private HolloutPermissions holloutPermissions;

    private Vibrator vibrator;

    private static int QUALITY_MODE = 0;

    private PhotosAndVideosAdapter photosAndVideosAdapter;

    private List<HolloutUtils.MediaEntry> allMediaEntries = new ArrayList<>();

    private BitmapDecodeTask bitmapDecodeTask;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);
        ButterKnife.bind(this);
        setupEmojiPopup();
        random = new Random();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        useCameraIconView.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_camera)
                .sizeDp(16).color(Color.WHITE));

        setToExpandIcon();

        doneWithContentSelection = findViewById(R.id.done_with_contents_selection);
        contentFlipper.setInAnimation(this, R.anim.animation_toggle_in);
        contentFlipper.setOutAnimation(this, R.anim.animation_toggle_out);

        doneWithContentSelection.hide();

        openEmojiView.setOnClickListener(this);
        changeStoryBoardColorView.setOnClickListener(this);
        changeTypefaceView.setOnClickListener(this);
        cameraContainerBackgroundView.setOnClickListener(this);
        holloutPermissions = new HolloutPermissions(this, rootView);

        initCamera();

        photosAndVideosAdapter = new PhotosAndVideosAdapter(this, allMediaEntries);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        moreMediaRecyclerView.setLayoutManager(gridLayoutManager);
        moreMediaRecyclerView.setHasFixedSize(true);
        moreMediaRecyclerView.setAdapter(photosAndVideosAdapter);

        loadRecentPhotos();

        dragPhotoTrailUpIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }

        });

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                UiUtils.showView(bottomBar, slidingUpPanelLayout.getPanelState()
                        == SlidingUpPanelLayout.PanelState.COLLAPSED);

                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    dragView.setBackgroundColor(ContextCompat.getColor(CreateStoryActivity.this, R.color.colorPrimary));
                    setToLessIcon();
                } else {
                    dragView.setBackgroundColor(Color.BLACK);
                    setToExpandIcon();
                }

            }

        });

        toggleMediaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchMoreMedia(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }

    private void setToExpandIcon() {
        dragPhotoTrailUpIcon.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_expand_less)
                .sizeDp(18).color(Color.WHITE));
    }

    private void setToLessIcon() {
        dragPhotoTrailUpIcon.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_expand_more)
                .sizeDp(18).color(Color.WHITE));
    }

    private void fetchMoreMedia(int position) {
        List<HolloutUtils.MediaEntry> mediaEntries = position == 0 ? fetchPhotos() : fetchVideos();
        allMediaEntries.clear();
        allMediaEntries.addAll(mediaEntries);
        if (photosAndVideosAdapter != null) {
            photosAndVideosAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<HolloutUtils.MediaEntry> fetchVideos() {
        return HolloutUtils.getSortedVideos(this);
    }

    private ArrayList<HolloutUtils.MediaEntry> fetchPhotos() {
        return HolloutUtils.getSortedPhotos(this);
    }

    public int dpToPx(int dp) {
        float density = getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        slidingUpPanelLayout.setPanelHeight(dpToPx(250));
    }

    public void vibrateVibrator() {
        vibrator.vibrate(100);
    }

    private void loadRecentPhotos() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionsUtils.checkSelfForStoragePermission(this)) {
            holloutPermissions.requestStoragePermissions();
            return;
        }
        recentPhotoViewRail.fetchRecentImages();
        recentPhotoViewRail.setListener(new RecentPhotoViewRail.OnItemClickedListener() {
            @Override
            public void onItemClicked(Uri uri) {
                handleSelectedFile(uri);
            }

        });
        fetchMoreMedia(0);
    }

    public void handleSelectedFile(Uri uri) {
        saveLastFlipperIndex();
        if (bitmapDecodeTask != null) {
            bitmapDecodeTask.cancel(true);
            bitmapDecodeTask = null;
        }
        final ProgressDialog progressDialog = ProgressDialog.show(CreateStoryActivity.this, null, "Please wait...");
        bitmapDecodeTask = new BitmapDecodeTask(CreateStoryActivity.this, new DoneCallback<Bitmap>() {
            @Override
            public void done(final Bitmap result, final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UiUtils.dismissProgressDialog(progressDialog);
                        if (e == null && result != null) {
                            UiUtils.toggleFlipperState(contentFlipper, 2);
//                                    PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(CreateStoryActivity.this, result);
//                                    filtersRecyclerView.setLayoutManager(new LinearLayoutManager(CreateStoryActivity.this, LinearLayoutManager.HORIZONTAL, false));
//                                    filtersRecyclerView.setAdapter(photoFiltersAdapter);
                            imagePreview.setImageBitmap(result);
                        }
                    }
                });
            }
        });
        bitmapDecodeTask.execute(uri);
    }

    private static Bitmap uriToBitmap(Context context, Uri selectedFileUri) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedFileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class BitmapDecodeTask extends AsyncTask<Uri, Void, Bitmap> {

        private DoneCallback<Bitmap> bitmapDoneCallback;
        private WeakReference<Context> weakReference;

        BitmapDecodeTask(Context context, DoneCallback<Bitmap> bitmapDoneCallback) {
            this.weakReference = new WeakReference<>(context);
            this.bitmapDoneCallback = bitmapDoneCallback;
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            if (weakReference.get() == null) {
                return null;
            }
            return uriToBitmap(weakReference.get(), uris[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            bitmapDoneCallback.done(bitmap, null);
        }
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
            public void onPictureCaptured(byte[] bytesArray) {
                //Write Bitmap to file
                saveBitmapToFileOutPut(bytesArray);
            }

            @Override
            public void setVideoCaptureStarted(long captureDownTime) {
                vibrateVibrator();
                UiUtils.showView(videoStartedTimer, true);
            }

        });
    }

    static Bitmap decodeBitmap(byte[] data) {
        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false; // Disable Dithering mode
        bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
        // memory, the Bitmap can be cleared
        bfOptions.inInputShareable = true; // Which kind of reference will be
        // used to recover the Bitmap data
        // after being clear, when it will
        // be used in the future
        bfOptions.inTempStorage = new byte[32 * 1024];
        if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bfOptions);
        return bitmap;
    }

    @Nullable
    private void saveBitmapToFileOutPut(byte[] bytes) {
        byte[] data = bytes;
        Bitmap bmp = decodeBitmap(data);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bmp != null && QUALITY_MODE == 0)
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        else if (bmp != null && QUALITY_MODE != 0)
            bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, byteArrayOutputStream);

        File imagesFolder = HolloutUtils.getFilePath(RandomStringUtils.random(5, true, true) + System.currentTimeMillis() + ".jpg", this, "image", false);
        FileOutputStream fo;
        try {
            fo = new FileOutputStream(imagesFolder);
            fo.write(byteArrayOutputStream.toByteArray());
            fo.close();
            if (Build.VERSION.SDK_INT < 19)
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            else {
                MediaScannerConnection
                        .scanFile(
                                getApplicationContext(),
                                new String[]{imagesFolder.toString()},
                                null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(
                                            String path, Uri uri) {
                                        HolloutLogger.i("ExternalStorage", "Scanned "
                                                + path + ":");
                                        HolloutLogger.i("ExternalStorage", "-> uri="
                                                + uri);
                                    }
                                });
            }
        } catch (FileNotFoundException e) {
            HolloutLogger.e("TAG", "FileNotFoundException" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                saveLastFlipperIndex();
                UiUtils.toggleFlipperState(contentFlipper, 1);
                break;
        }
    }

    private void randomizeColor() {
        int randomCol = UiUtils.getRandomColor();
        rootView.setBackgroundColor(randomCol);
        cameraContainerBackgroundView.setBackgroundColor(randomCol);
        tintToolbarAndTabLayout(randomCol);
    }

    private void tintToolbarAndTabLayout(int colorPrimary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(colorPrimary);
        }
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            return;
        }
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }
        int lastIndex = HolloutPreferences.getIndexOfLastDisplayedChild();
        if (contentFlipper.getDisplayedChild() != lastIndex) {
            contentFlipper.setDisplayedChild(lastIndex);
            return;
        }
        HolloutPreferences.saveLastFlipperIndexInStoryActivity(0);
        super.onBackPressed();
    }

    @Override
    public void onEventAsync(final Object o) {
        super.onEventAsync(o);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (o instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) o;
                    imagePreview.setImageBitmap(bitmap);
                } else if (o instanceof SelectedFileUriEvent) {
                    SelectedFileUriEvent selectedFileUriEvent = (SelectedFileUriEvent) o;
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    Uri uri = selectedFileUriEvent.getUri();
                    if (uri != null) {
                        handleSelectedFile(uri);
                    }
                }
            }
        });
    }

    private void saveLastFlipperIndex() {
        HolloutPreferences.saveLastFlipperIndexInStoryActivity(contentFlipper.getDisplayedChild());
    }
}
