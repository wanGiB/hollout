package com.wan.hollout.ui.activities;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.api.JsonApiClient;
import com.wan.hollout.ui.adapters.SlidePagerAdapter;
import com.wan.hollout.ui.widgets.PageIndicator;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bolts.Capture;

public class SlidePagerActivity extends AppCompatActivity {

    private FloatingActionButton likePicture;
    private ParseObject signedInUser;

    private DatabaseReference userPhotosReference;
    private ValueEventListener valueEventListener;

    private ArrayList<String> receivedPics;
    private String selectedPic;

    private Capture<String> userFirebaseCapture = new Capture<>();

    private List<HashMap<String, Object>> userPhotoLikes = new ArrayList<>();
    private HashMap<String, String> photoLikesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_pager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        showUi();

        final ViewPager pager = findViewById(R.id.pager);
        likePicture = findViewById(R.id.like_picture);

        SlidePagerAdapter pagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        if (getIntent() == null) return;
        String title = getIntent().getStringExtra(AppConstants.EXTRA_TITLE);

        String userId = getIntent().getStringExtra(AppConstants.EXTRA_USER_ID);
        signedInUser = AuthUtil.getCurrentUser();

        if (signedInUser != null) {
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(userId)) {
                getSupportActionBar().setTitle("Me");
                UiUtils.showView(likePicture, false);
            } else {

                userPhotosReference = FirebaseUtils.getPhotoLikesReference().child(userId);

                loadUserPhotosLikes();
                if (StringUtils.isNotEmpty(title)) {
                    getSupportActionBar().setTitle(StringUtils.capitalize(title));
                } else {
                    getSupportActionBar().setTitle("Profile Photos");
                }

                likePicture.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        HashMap<String, Object> likeProps = new HashMap<>();
                        likeProps.put("photo_url", selectedPic);
                        likeProps.put("liker", signedInUser.getString(AppConstants.REAL_OBJECT_ID));
                        String newLikeHash = HolloutUtils.hashString(likeProps.toString());
                        if (!userPhotoLikes.contains(likeProps)) {
                            likeProps.put("createdAt", System.currentTimeMillis());
                            userPhotosReference.push().setValue(likeProps);
                            setPhotoToLiked(ColorStateList.valueOf(ContextCompat.getColor(SlidePagerActivity.this,
                                    R.color.colorGoogle)));
                            if (photoLikesMap.containsKey(newLikeHash)) {
                                photoLikesMap.remove(newLikeHash);
                            }
                            //Send Push notification to user that I liked his photo
                            if (userFirebaseCapture != null && userFirebaseCapture.get() != null) {
                                JsonApiClient.sendFirebasePushNotification(userFirebaseCapture.get(), AppConstants.NOTIFICATION_TYPE_PHOTO_LIKE);
                            }
                        } else {
                            //user already liked this photo
                            //Remove Photo
                            String photoKey;
                            if (photoLikesMap.containsKey(newLikeHash)) {
                                photoKey = photoLikesMap.get(newLikeHash);
                                userPhotoLikes.remove(likeProps);
                                userPhotosReference.child(photoKey).removeValue();
                            }
                            checkLiked(selectedPic);
                        }
                    }
                });
                final ParseQuery<ParseObject> parseObjectParseQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
                parseObjectParseQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, userId);
                parseObjectParseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null && object != null) {
                            String userFirebaseToken = object.getString(AppConstants.USER_FIREBASE_TOKEN);
                            if (StringUtils.isNotEmpty(userFirebaseToken)) {
                                userFirebaseCapture.set(userFirebaseToken);
                            }
                        }
                        parseObjectParseQuery.cancel();
                    }
                });
            }

            // set pictures
            receivedPics = getIntent().getStringArrayListExtra(AppConstants.EXTRA_PICTURES);
            selectedPic = receivedPics.get(0);
            checkLiked(selectedPic);
            pagerAdapter.addAll(receivedPics);
            pager.setAdapter(pagerAdapter);

            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    selectedPic = receivedPics.get(position);
                    checkLiked(selectedPic);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }

            });

            PageIndicator mPageIndicator = findViewById(R.id.indicator);
            mPageIndicator.setIndicatorType(PageIndicator.IndicatorType.FRACTION);
            mPageIndicator.setViewPager(pager);
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == 0) {
                        getSupportActionBar().show();
                    }
                }
            });
        }
    }

    private void checkLiked(String selectedPic) {
        HashMap<String, Object> selectedPicProps = new HashMap<>();
        selectedPicProps.put("liker", signedInUser.getString(AppConstants.REAL_OBJECT_ID));
        selectedPicProps.put("photo_url", selectedPic);
        if (userPhotoLikes != null && userPhotoLikes.contains(selectedPicProps)) {
            setPhotoToLiked(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGoogle)));
        } else {
            setPhotoToUnLiked(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        }
    }

    private void setPhotoToUnLiked(ColorStateList colorStateList) {
        likePicture.setImageResource(R.drawable.like);
        likePicture.setBackgroundTintList(colorStateList);
    }

    private void setPhotoToLiked(ColorStateList tint) {
        likePicture.setImageResource(R.drawable.like_a_picture);
        likePicture.setBackgroundTintList(tint);
    }

    private void loadUserPhotosLikes() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new
                            GenericTypeIndicator<HashMap<String, Object>>() {
                            };
                    HashMap<String, Object> photoLike = snapshot.getValue(genericTypeIndicator);
                    if (photoLike != null) {

                        if (photoLike.containsKey(AppConstants.SEEN_BY_OWNER)) {
                            photoLike.remove(AppConstants.SEEN_BY_OWNER);
                        }

                        if (photoLike.containsKey(AppConstants.PREVIEWED)) {
                            photoLike.remove(AppConstants.PREVIEWED);
                        }

                        if (photoLike.containsKey("createdAt")) {
                            photoLike.remove("createdAt");
                        }

                        userPhotoLikes.add(photoLike);
                        photoLikesMap.put(HolloutUtils.hashString(photoLike.toString()), snapshot.getKey());
                    }
                }
                checkLiked(selectedPic);
                HolloutLogger.d("PhotoLikes", userPhotoLikes.toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if (userPhotosReference != null) {
            userPhotosReference.addValueEventListener(valueEventListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userPhotosReference != null && valueEventListener != null) {
            userPhotosReference.removeEventListener(valueEventListener);
        }
        photoLikesMap.clear();
        userPhotoLikes.clear();
        receivedPics.clear();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            showUi();
        } else {
            hideUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            toggleActionBar();
        }
        return true;
    }

    private void toggleActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (actionBar.isShowing()) {
                actionBar.hide();
                hideUi();
            } else {
                showUi();
                actionBar.show();
            }
        }
    }

    private void showUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        } else {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void hideUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
