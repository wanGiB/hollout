package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.ui.activities.SlidePagerActivity;
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

public class FeaturedPhotosRectangleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Activity activity;
    private List<String> photos;
    private String username;
    private String userId;

    public FeaturedPhotosRectangleAdapter(Activity activity, List<String> photos, String username, String userId) {
        this.activity = activity;
        this.photos = photos;
        this.username = username;
        this.userId = userId;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View photoView = layoutInflater.inflate(R.layout.rectangular_featured_photo_item, parent, false);
        return new FeaturedPhotosRectangleAdapter.PhotoItemHolder(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            final FeaturedPhotosRectangleAdapter.PhotoItemHolder photoItemHolder = (FeaturedPhotosRectangleAdapter.PhotoItemHolder) holder;
            final String photo = photos.get(position);
            if (StringUtils.isNotEmpty(photo)) {
                if (photoItemHolder.userPhotoView != null) {
                    UiUtils.loadImage(activity, photo, photoItemHolder.userPhotoView);

                    photoItemHolder.userPhotoView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            UiUtils.blinkView(v);
                            Intent mProfilePhotoViewIntent = new Intent(activity, SlidePagerActivity.class);
                            mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_TITLE, username);
                            mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_USER_ID, userId);
                            ArrayList<String> photoExtras = new ArrayList<>();
                            photoExtras.add(0, photo);
                            for (String photoItem : photos) {
                                if (!photoExtras.contains(photoItem) && !photoItem.equals(photo)) {
                                    photoExtras.add(photoItem);
                                }
                            }
                            mProfilePhotoViewIntent.putStringArrayListExtra(AppConstants.EXTRA_PICTURES, photoExtras);
                            activity.startActivity(mProfilePhotoViewIntent);
                        }

                    });

                    photoItemHolder.userPhotoView.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View view) {
                            if (signedInUserObject.getString(AppConstants.REAL_OBJECT_ID).equals(userId)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage("Delete and un-feature photo");
                                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        List<String> featuredPhotos = signedInUserObject.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
                                        if (featuredPhotos != null && featuredPhotos.contains(photo)) {
                                            featuredPhotos.remove(photo);
                                            final ProgressDialog progressDialog = UiUtils.showProgressDialog(activity, "Un-Featuring photo...");
                                            signedInUserObject.put(AppConstants.APP_USER_FEATURED_PHOTOS, featuredPhotos);
                                            AuthUtil.updateCurrentLocalUser(signedInUserObject, new DoneCallback<Boolean>() {
                                                @Override
                                                public void done(Boolean result, Exception e) {
                                                    if (e == null) {
                                                        UiUtils.dismissProgressDialog(progressDialog);
                                                        UiUtils.showSafeToast("Deleted and un-featured successfully");
                                                        notifyDataSetChanged();
                                                        //Remove all references to this photo on firebase
                                                        final DatabaseReference userDatabaseRef = FirebaseUtils
                                                                .getPhotoLikesReference().child(signedInUserObject
                                                                        .getString(AppConstants.REAL_OBJECT_ID));

                                                        userDatabaseRef
                                                                .orderByChild("photo_url")
                                                                .equalTo(photo)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {

                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {

                                                                            GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new
                                                                                    GenericTypeIndicator<HashMap<String, Object>>() {
                                                                                    };

                                                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                HashMap<String, Object> photoLike = snapshot.getValue(genericTypeIndicator);
                                                                                if (photoLike != null && photoLike.containsValue(photo)) {
                                                                                    String key = snapshot.getKey();
                                                                                    if (key != null) {
                                                                                        userDatabaseRef.child(key).removeValue();
                                                                                    }
                                                                                }
                                                                            }

                                                                            ParseQuery<ParseObject> parseObjectParseQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
                                                                            parseObjectParseQuery.whereEqualTo(AppConstants.LIKED_PHOTO, photo);
                                                                            parseObjectParseQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_PHOTO_LIKE);
                                                                            parseObjectParseQuery.findInBackground(new FindCallback<ParseObject>() {
                                                                                @Override
                                                                                public void done(List<ParseObject> objects, ParseException e) {
                                                                                    if (e == null && objects != null && !objects.isEmpty()) {
                                                                                        ParseObject.deleteAllInBackground(objects);
                                                                                    }
                                                                                }
                                                                            });
                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }

                                                                });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.create().show();
                            }
                            return true;
                        }

                    });

                }

            }

        }

    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class PhotoItemHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.featured_photo_item)
        ImageView userPhotoView;

        public PhotoItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
