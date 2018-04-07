package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.sliding.SlidingActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.EndlessRecyclerViewScrollListener;
import com.wan.hollout.ui.adapters.PhotoLikesAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PhotoLikesActivity extends SlidingActivity {

    @BindView(R.id.nothing_to_load)
    HolloutTextView nothingToLoadView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.photo_likes_recycler_view)
    RecyclerView photoLikesRecyclerView;

    private PhotoLikesAdapter photoLikesAdapter;
    private List<ParseObject> photoLikes = new ArrayList<>();

    private ParseObject signedInUserObject;

    @Override
    public void init(Bundle savedInstanceState) {
        setTitle("Likes");
        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );
        setContent(R.layout.activity_photo_likes);
        ButterKnife.bind(this);

        signedInUserObject = AuthUtil.getCurrentUser();
        photoLikesAdapter = new PhotoLikesAdapter(this, photoLikes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        photoLikesRecyclerView.setLayoutManager(linearLayoutManager);
        photoLikesRecyclerView.setAdapter(photoLikesAdapter);
        fetchMyPhotoLikesFromFirebase();
        fetchMyPhotoLikesFromParse(0);
        photoLikesRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                fetchMyPhotoLikesFromParse(photoLikes.size());
            }
        });
    }

    public void fetchMyPhotoLikesFromFirebase() {
        if (signedInUserObject != null) {
            final String signedInUserId = signedInUserObject.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(signedInUserId)) {
                FirebaseUtils.getPhotoLikesReference().child(signedInUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.exists()) {
                                    long dataSnapShotCount = dataSnapshot.getChildrenCount();
                                    if (dataSnapShotCount != 0) {
                                        GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new
                                                GenericTypeIndicator<HashMap<String, Object>>() {
                                                };
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            HashMap<String, Object> photoLike = snapshot.getValue(genericTypeIndicator);
                                            if (photoLike != null) {
                                                Boolean photoPreviewed = (Boolean) photoLike.get(AppConstants.PREVIEWED);
                                                if (photoPreviewed == null) {
                                                    markAsPreviewed(snapshot, signedInUserId);
                                                } else {
                                                    if (!photoPreviewed) {
                                                        markAsPreviewed(snapshot, signedInUserId);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

            }
        }
    }

    private void markAsPreviewed(DataSnapshot snapshot, String signedInUserId) {
        HashMap<String, Object> updatableProps = new HashMap<>();
        updatableProps.put(AppConstants.PREVIEWED, true);
        FirebaseUtils.getPhotoLikesReference()
                .child(signedInUserId)
                .child(snapshot.getKey())
                .updateChildren(updatableProps);
    }

    private void fetchMyPhotoLikesFromParse(final int skip) {
        ParseQuery<ParseObject> photoLikesQuery = ParseQuery.getQuery(AppConstants.PHOTO_LIKES);
        photoLikesQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUserObject.getString(AppConstants.REAL_OBJECT_ID));
        photoLikesQuery.include(AppConstants.FEED_CREATOR);
        if (!photoLikes.isEmpty()) {
            photoLikesQuery.setSkip(skip);
        }
        photoLikesQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && !objects.isEmpty()) {
                    if (skip == 0) {
                        photoLikes.clear();
                    }
                    photoLikes.addAll(objects);
                    photoLikesAdapter.notifyDataSetChanged();
                }
                UiUtils.showView(nothingToLoadView, photoLikes.isEmpty());
                UiUtils.showView(progressBar, false);
            }
        });
    }

}
