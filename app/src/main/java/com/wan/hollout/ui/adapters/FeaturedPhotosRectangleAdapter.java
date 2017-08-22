package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseObject;

import com.parse.SaveCallback;
import com.wan.hollout.R;
import com.wan.hollout.ui.activities.SlidePagerActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View photoView = layoutInflater.inflate(R.layout.rectangular_featured_photo_item, parent, false);
        return new FeaturedPhotosRectangleAdapter.PhotoItemHolder(photoView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
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
                            if (signedInUserObject.getObjectId().equals(userId)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage("Delete and un-feature photo");
                                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        List<String> featuredPhotos = signedInUserObject.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
                                        if (featuredPhotos != null && featuredPhotos.contains(photo)) {
                                            featuredPhotos.remove(photo);
                                            UiUtils.showProgressDialog(activity,"Un-Featuring photo...");
                                            signedInUserObject.put(AppConstants.APP_USER_FEATURED_PHOTOS, featuredPhotos);
                                            signedInUserObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        UiUtils.dismissProgressDialog();
                                                        UiUtils.showSafeToast("Deleted and un-featured successfully");
                                                        notifyDataSetChanged();
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
