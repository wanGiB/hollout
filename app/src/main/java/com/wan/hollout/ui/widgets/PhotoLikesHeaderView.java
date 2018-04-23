package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.EndlessRecyclerViewScrollListener;
import com.wan.hollout.ui.adapters.FeedsAdapter;
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

public class PhotoLikesHeaderView extends LinearLayout {

    @BindView(R.id.photo_likes_count_header)
    HolloutTextView requestsCountHeaderView;

    @BindView(R.id.first_liker)
    CircleImageView firstLikerView;

    @BindView(R.id.second_liker)
    CircleImageView secondLikerView;

    @BindView(R.id.third_liker)
    CircleImageView thirdLikerView;

    @BindView(R.id.clickable_container)
    View clickableContainer;

    private List<ParseObject> photoLikes = new ArrayList<>();
    private FeedsAdapter feedsAdapter;

    public PhotoLikesHeaderView(Context context) {
        this(context, null);
    }

    public PhotoLikesHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoLikesHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.photo_likes_header_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setPhotoRequests(final Activity activity, List<ParseObject> photoLikes, int totalCount) {
        String message = "Somebody liked your photo";
        if (totalCount > 1) {
            message = totalCount + " people liked your photo";
        }
        requestsCountHeaderView.setText(message);

        if (photoLikes.size() == 1) {
            UiUtils.showView(secondLikerView, false);
            UiUtils.showView(thirdLikerView, false);
            UiUtils.showView(firstLikerView, true);
            bindData(activity, photoLikes.get(0), firstLikerView);
        } else if (photoLikes.size() == 2) {
            UiUtils.showView(secondLikerView, true);
            UiUtils.showView(thirdLikerView, false);
            UiUtils.showView(firstLikerView, true);
            bindData(activity, photoLikes.get(0), firstLikerView);
            bindData(activity, photoLikes.get(1), secondLikerView);
        } else {
            UiUtils.showView(secondLikerView, true);
            UiUtils.showView(thirdLikerView, true);
            UiUtils.showView(firstLikerView, true);
            bindData(activity, photoLikes.get(0), firstLikerView);
            bindData(activity, photoLikes.get(1), secondLikerView);
            bindData(activity, photoLikes.get(2), thirdLikerView);
        }

        handleClicks(activity);

    }

    private void handleClicks(final Activity activity) {
        clickableContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initBottomSheetDialog(activity);
            }
        });
    }

    public void attachEventHandlers(Activity activity) {
        handleClicks(activity);
    }

    public void bindData(Activity activity, ParseObject photoLike, CircleImageView requesterPhotoView) {
        ParseObject requestOriginator = photoLike.getParseObject(AppConstants.FEED_CREATOR);
        if (requestOriginator != null) {
            String userProfilePhotoUrl = requestOriginator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                UiUtils.loadImage(activity, userProfilePhotoUrl, requesterPhotoView);
            } else {
                requesterPhotoView.setImageResource(R.drawable.empty_profile);
            }
        }
    }

    private void initBottomSheetDialog(Activity activity) {
        @SuppressLint("InflateParams")
        View bottomView = LayoutInflater.from(activity).inflate(R.layout.photo_likes_recycler_view, null);
        final ProgressBar progressBar = bottomView.findViewById(R.id.progress_bar);
        RecyclerView photoLikesRecyclerView = bottomView.findViewById(R.id.photo_likes_recycler_view);
        feedsAdapter = new FeedsAdapter(activity, photoLikes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        photoLikesRecyclerView.setLayoutManager(linearLayoutManager);
        photoLikesRecyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        photoLikesRecyclerView.setAdapter(feedsAdapter);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.show();
        fetchPhotoLikes(progressBar, photoLikes.size());
        photoLikesRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!photoLikes.isEmpty() && photoLikes.size() >= 30) {
                    fetchPhotoLikes(progressBar, photoLikes.size());
                }
            }
        });
    }

    private void fetchPhotoLikes(final ProgressBar progressBar, int skip) {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            final ParseQuery<ParseObject> photoLikesQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            photoLikesQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUserObject.getString(AppConstants.REAL_OBJECT_ID));
            photoLikesQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_PHOTO_LIKE);
            if (skip != 0) {
                photoLikesQuery.setSkip(skip);
            }
            photoLikesQuery.setLimit(30);
            photoLikesQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    UiUtils.showView(progressBar, false);
                    if (e == null && objects != null && !objects.isEmpty()) {
                        photoLikes.addAll(objects);
                        feedsAdapter.notifyDataSetChanged();
                    }
                    photoLikesQuery.cancel();
                }
            });
        }
    }

}
