package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.chat.ChatUtils;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatRequestView extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.requester_photo)
    CircleImageView requesterPhotoView;

    @BindView(R.id.requester_name)
    HolloutTextView requesterNameView;

    @BindView(R.id.about_requester)
    HolloutTextView aboutRequesterView;

    @BindView(R.id.distance_to_requester)
    HolloutTextView distanceToRequesterView;

    @BindView(R.id.accept_request)
    TextView acceptRequestTextView;

    @BindView(R.id.decline_request)
    TextView declineRequestView;

    private ParseUser signedInUser;

    public ChatRequestView(Context context) {
        this(context, null);
    }

    public ChatRequestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRequestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.chat_request_item, this);
    }

    public void bindData(final Activity activity, final ChatRequestsAdapterView parent, final ParseObject feedObject) {
        this.signedInUser = ParseUser.getCurrentUser();
        if (feedObject != null) {
            String requestType = feedObject.getString(AppConstants.FEED_TYPE);
            if (requestType.equals(AppConstants.FEED_TYPE_CHAT_REQUEST)) {
                final ParseUser requestOriginator = feedObject.getParseUser(AppConstants.FEED_CREATOR);
                if (requestOriginator != null) {
                    final String userDisplayName = requestOriginator.getString(AppConstants.APP_USER_DISPLAY_NAME);
                    if (StringUtils.isNotEmpty(userDisplayName)) {
                        requesterNameView.setText(WordUtils.capitalize(userDisplayName));
                    }
                    String userProfilePhotoUrl = requestOriginator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                    if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                        UiUtils.loadImage(activity, userProfilePhotoUrl, requesterPhotoView);
                    } else {
                        requesterPhotoView.setImageResource(R.drawable.empty_profile);
                    }
                    List<String> aboutUser = requestOriginator.getList(AppConstants.ABOUT_USER);
                    List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);

                    if (aboutUser != null && aboutSignedInUser != null) {
                        try {
                            List<String> common = new ArrayList<>(aboutUser);
                            common.retainAll(aboutSignedInUser);
                            String firstInterest = !common.isEmpty() ? common.get(0) : aboutUser.get(0);
                            aboutRequesterView.setText(WordUtils.capitalize(firstInterest));
                        } catch (NullPointerException ignored) {

                        }
                    }
                    ParseGeoPoint userGeoPoint = requestOriginator.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
                    ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
                    if (signedInUserGeoPoint != null && userGeoPoint != null) {
                        double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                        String value = HolloutUtils.formatDistance(distanceInKills);
                        UiUtils.setTextOnView(distanceToRequesterView, value + "Km from you");
                    } else {
                        UiUtils.setTextOnView(distanceToRequesterView, " ");
                    }
                    acceptRequestTextView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            acceptRequestTextView.setText(activity.getString(R.string.working));
                            ChatUtils.acceptChatInvitation(requestOriginator.getUsername().toLowerCase(), new DoneCallback<Boolean>() {
                                @Override
                                public void done(Boolean result, Exception e) {
                                    if (e == null) {
                                        //remove request
                                        ParseQuery<ParseObject> requestObjectQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
                                        requestObjectQuery.whereEqualTo(AppConstants.OBJECT_ID, feedObject.getObjectId());
                                        requestObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(final ParseObject returnedFeedObject, ParseException e) {
                                                List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
                                                if (signedInUserChats != null && !signedInUserChats.contains(requestOriginator.getObjectId())) {
                                                    signedInUserChats.add(requestOriginator.getObjectId());
                                                } else {
                                                    signedInUserChats = new ArrayList<>();
                                                    signedInUserChats.add(requestOriginator.getObjectId());
                                                }
                                                signedInUser.put(AppConstants.APP_USER_CHATS, signedInUserChats);
                                                signedInUser.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            if (returnedFeedObject != null) {
                                                                returnedFeedObject.deleteInBackground(new DeleteCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e == null) {
                                                                            UiUtils.snackMessage("Request from " + userDisplayName + " successfully accepted.", ChatRequestView.this, true, null, null);
                                                                            removeRequest(parent, feedObject);
                                                                            EventBus.getDefault().post(AppConstants.REFRESH_CONVERSATIONS);
                                                                        } else {
                                                                            UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
                                                            }
                                                        } else {
                                                            UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
                                    }
                                }
                            });
                        }
                    });

                    declineRequestView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            declineRequestView.setText(activity.getString(R.string.working));
                            ChatUtils.declineChatInvitation(requestOriginator.getUsername(), new DoneCallback<Boolean>() {
                                @Override
                                public void done(Boolean declined, Exception e) {
                                    if (e == null && declined) {
                                        ParseQuery<ParseObject> requestObjectQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
                                        requestObjectQuery.whereEqualTo(AppConstants.OBJECT_ID, feedObject.getObjectId());
                                        requestObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(final ParseObject object, ParseException e) {
                                                if (e == null && object != null) {
                                                    object.deleteInBackground(new DeleteCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            UiUtils.snackMessage("Request from " + userDisplayName + " declined successfully.", ChatRequestView.this, true, null, null);
                                                            removeRequest(parent, object);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            } else if (requestType.equals(AppConstants.FEED_TYPE_JOIN_GROUP_REQUEST)) {
                ParseObject groupDetails = feedObject.getParseObject(AppConstants.GROUP_OR_ROOM);
            }
        }
    }

    private void removeRequest(ChatRequestsAdapterView parent, ParseObject parseObject) {
        if (parent != null) {
            parent.removeChatRequest(parseObject);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private void init() {
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}
