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

    private HashMap<String, List<HashMap<String, Object>>> idObjectMap = new HashMap<>();
    private PhotoLikesAdapter photoLikesAdapter;
    private List<ParseObject> photoLikes = new ArrayList<>();

    @Override
    public void init(Bundle savedInstanceState) {
        setTitle("Likes");
        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );
        setContent(R.layout.activity_photo_likes);
        ButterKnife.bind(this);
        photoLikesAdapter = new PhotoLikesAdapter(this, photoLikes);
        photoLikesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        photoLikesRecyclerView.setAdapter(photoLikesAdapter);
        fetchMyPhotoLikes();
    }

    public void fetchMyPhotoLikes() {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
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
                                        UiUtils.showView(nothingToLoadView, false);
                                        GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new
                                                GenericTypeIndicator<HashMap<String, Object>>() {
                                                };
                                        List<String> idsOfPhotoLikes = new ArrayList<>();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            HashMap<String, Object> photoLike = snapshot.getValue(genericTypeIndicator);
                                            if (photoLike != null) {

                                                String likerId = (String) photoLike.get("liker");

                                                List<HashMap<String, Object>> idMapList = idObjectMap.get(likerId);

                                                if (idMapList == null) {
                                                    idMapList = new ArrayList<>();
                                                }

                                                if (!idMapList.contains(photoLike)) {
                                                    idMapList.add(photoLike);
                                                    idObjectMap.put(likerId, idMapList);
                                                }

                                                if (!idsOfPhotoLikes.contains(likerId)) {
                                                    idsOfPhotoLikes.add(likerId);
                                                }

                                                Boolean photoPreviewed = (Boolean) photoLike.get(AppConstants.PREVIEWED);

                                                if (photoPreviewed != null && !photoPreviewed) {
                                                    HashMap<String, Object> updatableProps = new HashMap<>();
                                                    updatableProps.put(AppConstants.PREVIEWED, true);
                                                    FirebaseUtils.getPhotoLikesReference()
                                                            .child(signedInUserId)
                                                            .child(snapshot.getKey())
                                                            .updateChildren(updatableProps);
                                                }

                                            }

                                        }
                                        if (!idsOfPhotoLikes.isEmpty()) {
                                            fetchPhotoLikesFromParse(idsOfPhotoLikes);
                                        } else {
                                            UiUtils.showView(nothingToLoadView, true);
                                            UiUtils.showView(progressBar, false);
                                        }
                                    } else {
                                        UiUtils.showView(nothingToLoadView, true);
                                        UiUtils.showView(progressBar, false);
                                    }
                                } else {
                                    UiUtils.showView(nothingToLoadView, true);
                                    UiUtils.showView(progressBar, false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                UiUtils.showView(nothingToLoadView, true);
                                UiUtils.showView(progressBar, false);
                            }

                        });

            }
        }
    }

    private void fetchPhotoLikesFromParse(List<String> idsOfPhotoLikers) {
        ParseQuery<ParseObject> parseObjectParseQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        parseObjectParseQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, idsOfPhotoLikers);
        parseObjectParseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && !objects.isEmpty()) {
                    photoLikes.clear();
                    for (ParseObject parseObject : objects) {
                        String realObjectId = parseObject.getString(AppConstants.REAL_OBJECT_ID);
                        List<HashMap<String, Object>> photoLikesListByUser = idObjectMap.get(realObjectId);
                        HashMap<String, Object> objectHashMap = photoLikesListByUser.get(0);

                        //Get the properties of the current photo like
                        long createdAt = (long) objectHashMap.get("createdAt");
                        Boolean seenByOwner = (Boolean) objectHashMap.get(AppConstants.SEEN_BY_OWNER);
                        String photoUrl = (String) objectHashMap.get("photo_url");

                        parseObject.put(AppConstants.PHOTO_LIKE_DATE, createdAt);
                        if (seenByOwner != null) {
                            parseObject.put(AppConstants.SEEN_BY_OWNER, seenByOwner);
                        }
                        parseObject.put(AppConstants.LIKED_PHOTO, photoUrl);

                        photoLikes.add(parseObject);

                        photoLikesListByUser.remove(objectHashMap);
                        idObjectMap.put(realObjectId, photoLikesListByUser);
                    }
                    photoLikesAdapter.notifyDataSetChanged();
                    UiUtils.showView(nothingToLoadView, false);
                    UiUtils.showView(progressBar, false);
                } else {
                    UiUtils.showView(nothingToLoadView, true);
                    UiUtils.showView(progressBar, false);
                }
            }
        });
    }

}
