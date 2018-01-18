package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.activities.FullChatRequestsActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatRequestsHeaderView extends LinearLayout {

    @BindView(R.id.requests_count_header)
    HolloutTextView requestsCountHeaderView;

    @BindView(R.id.nearby_header_view)
    HolloutTextView nearbyHeaderView;

    @BindView(R.id.first_requester)
    CircleImageView firstRequesterView;

    @BindView(R.id.second_requester)
    CircleImageView secondRequesterView;

    @BindView(R.id.third_requester)
    CircleImageView thirdRequesterView;

    @BindView(R.id.clickable_container)
    View clickableContainer;

    public ChatRequestsHeaderView(Context context) {
        this(context, null);
    }

    public ChatRequestsHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRequestsHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.chat_requests_adapter_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setChatRequests(final Activity activity, List<ParseObject> chatRequests, int totalCount) {
        String message = "1 New Chat Request";
        if (totalCount > 1) {
            message = totalCount + " New Chat Requests";
        }
        requestsCountHeaderView.setText(message);

        if (chatRequests.size() == 1) {
            UiUtils.showView(secondRequesterView, false);
            UiUtils.showView(thirdRequesterView, false);
            UiUtils.showView(firstRequesterView, true);
            bindData(activity, chatRequests.get(0), firstRequesterView);
        } else if (chatRequests.size() == 2) {
            UiUtils.showView(secondRequesterView, true);
            UiUtils.showView(thirdRequesterView, false);
            UiUtils.showView(firstRequesterView, true);
            bindData(activity, chatRequests.get(0), firstRequesterView);
            bindData(activity, chatRequests.get(1), secondRequesterView);
        } else {
            UiUtils.showView(secondRequesterView, true);
            UiUtils.showView(thirdRequesterView, true);
            UiUtils.showView(firstRequesterView, true);
            bindData(activity, chatRequests.get(0), firstRequesterView);
            bindData(activity, chatRequests.get(1), secondRequesterView);
            bindData(activity, chatRequests.get(2), thirdRequesterView);
        }

        handleClicks(activity);

    }

    private void handleClicks(final Activity activity) {
        clickableContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.showView(ChatRequestsHeaderView.this, false);
                Intent fullChatRequests = new Intent(activity, FullChatRequestsActivity.class);
                activity.startActivity(fullChatRequests);
            }
        });
    }

    public void attachEventHandlers(Activity activity) {
        handleClicks(activity);
    }

    @SuppressWarnings("SameParameterValue")
    public void showNearbyHeader(boolean canShowNearbyHeader) {
        UiUtils.showView(nearbyHeaderView, canShowNearbyHeader);
    }

    public void bindData(Activity activity, ParseObject chatRequest, CircleImageView requesterPhotoView) {
        ParseObject requestOriginator = chatRequest.getParseObject(AppConstants.FEED_CREATOR);
        if (requestOriginator != null) {
            String userProfilePhotoUrl = requestOriginator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                UiUtils.loadImage(activity, userProfilePhotoUrl, requesterPhotoView);
            } else {
                requesterPhotoView.setImageResource(R.drawable.empty_profile);
            }
        }
    }

}
