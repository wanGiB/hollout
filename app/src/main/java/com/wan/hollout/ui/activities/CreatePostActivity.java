package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.parse.ParseObject;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.wan.hollout.R;
import com.wan.hollout.ui.adapters.PhotosAndVideosAdapter;
import com.wan.hollout.ui.adapters.SelectedFilesAdapter;
import com.wan.hollout.ui.widgets.StoryBox;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class CreatePostActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_TAKE_VIDEO = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.open_emoji)
    ImageView openEmojiView;

    @BindView(R.id.add_attachment)
    ImageView addAttachments;

    @BindView(R.id.change_color)
    ImageView changeStoryBoardColorView;

    @BindView(R.id.change_typeface)
    ImageView changeTypefaceView;

    @BindView(R.id.rootLayout)
    LinearLayout rootView;

    @BindView(R.id.story_box)
    StoryBox storyBox;

    @BindView(R.id.dragView)
    View dragView;

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    @BindView(R.id.more_media_recycler_view)
    RecyclerView moreMediaRecyclerView;

    @BindView(R.id.toggle_media)
    Spinner toggleMediaSpinner;

    @BindView(R.id.composeAvatar)
    ImageView userAvatar;

    @BindView(R.id.activity_compose)
    View composeContainer;

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.selected_files_for_upload)
    RecyclerView selectedFilesForUpload;

    @SuppressLint("StaticFieldLeak")
    public static ImageView doneWithContentSelection;

    private EmojiPopup emojiPopup;
    private Random random;
    private HolloutPermissions holloutPermissions;
    private Vibrator vibrator;
    private PhotosAndVideosAdapter photosAndVideosAdapter;
    private List<HolloutUtils.MediaEntry> allMediaEntries = new ArrayList<>();

    private SelectedFilesAdapter selectedFilesAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupEmojiPopup();
        random = new Random();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Drawable doneIcon = new IconicsDrawable(this, GoogleMaterial.Icon.gmd_done).sizeDp(24).color(Color.WHITE);
        doneWithContentSelection = findViewById(R.id.done_with_contents_selection);
        doneWithContentSelection.setImageDrawable(doneIcon);
        UiUtils.showView(doneWithContentSelection, false);
        openEmojiView.setOnClickListener(this);
        changeStoryBoardColorView.setOnClickListener(this);
        changeTypefaceView.setOnClickListener(this);
        holloutPermissions = new HolloutPermissions(this, rootView);

        photosAndVideosAdapter = new PhotosAndVideosAdapter(this, allMediaEntries);

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        moreMediaRecyclerView.setLayoutManager(gridLayoutManager);
        moreMediaRecyclerView.setHasFixedSize(true);
        moreMediaRecyclerView.setAdapter(photosAndVideosAdapter);

        loadMedia();

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    checkAndShowSelectedFiles();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        }
                    }, 100);
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

        addAttachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.dismissKeyboard(storyBox);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                if (photosAndVideosAdapter != null) {
                    photosAndVideosAdapter.notifyDataSetChanged();
                }
            }
        });

        Drawable closeDrawable = new IconicsDrawable(this, GoogleMaterial.Icon.gmd_close).color(Color.WHITE).sizeDp(24);
        closeActivityView.setImageDrawable(closeDrawable);

        closeActivityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadUserPhoto();

        doneWithContentSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndShowSelectedFiles();
            }
        });

        checkAndShowSelectedFiles();
    }

    private void checkAndShowSelectedFiles() {
        if (!AppConstants.selectedUris.isEmpty()) {
            UiUtils.showView(selectedFilesForUpload, true);
            selectedFilesAdapter = new SelectedFilesAdapter(CreatePostActivity.this);
            selectedFilesForUpload.setLayoutManager(new LinearLayoutManager(CreatePostActivity.this, LinearLayoutManager.HORIZONTAL, false));
            selectedFilesForUpload.setAdapter(selectedFilesAdapter);
        }
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        MenuItem continueButton = menu.findItem(R.id.button_continue);
        continueButton.setVisible(false);
        filterPeopleMenuItem.setVisible(false);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadUserPhoto() {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            String signedInUserPhotoUrl = signedInUserObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(signedInUserPhotoUrl)) {
                UiUtils.loadImage(this, signedInUserPhotoUrl, userAvatar);
            }
        }
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

    private void loadMedia() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionsUtils.checkSelfForStoragePermission(this)) {
            holloutPermissions.requestStoragePermissions();
            return;
        }
        fetchMoreMedia(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadMedia();
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

        storyBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (StringUtils.isNotEmpty(s.toString().trim())){
                    storyBox.setCursorVisible(true);
                }else {
                    storyBox.setCursorVisible(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        }
    }

    private void randomizeColor() {
        int randomCol = UiUtils.getRandomColor();
        rootView.setBackgroundColor(ContextCompat.getColor(this, randomCol));
        composeContainer.setBackgroundColor(ContextCompat.getColor(this, randomCol));
        tintToolbarAndTabLayout(randomCol);
    }

    private void tintToolbarAndTabLayout(int colorPrimary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, colorPrimary));
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
        super.onBackPressed();
    }

    public void initPhotoCapture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                AppConstants.selectedUris.add(uri);
                checkAndShowSelectedFiles();
            }
        } else if (requestCode == REQUEST_TAKE_VIDEO && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                String realUri = getRealPathFromURI(uri);
                if (realUri != null) {
                    Uri videoUri = Uri.parse(realUri);
                    AppConstants.selectedUris.add(videoUri);
                    checkAndShowSelectedFiles();
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }

    public void shootVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        File videoOutputFile = HolloutUtils.getOutputMediaFile(AppConstants.CAPTURE_MEDIA_TYPE_VIDEO);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoOutputFile);
        startActivityForResult(intent, REQUEST_TAKE_VIDEO);
    }

}
