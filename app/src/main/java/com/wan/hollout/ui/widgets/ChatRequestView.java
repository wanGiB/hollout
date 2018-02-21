package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.ui.adapters.ChatRequestsAdapter;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wan Clem
 */

public class ChatRequestView extends LinearLayout /*implements View.OnClickListener, View.OnLongClickListener*/ {

    private CircleImageView requesterPhotoView;
    private HolloutTextView requesterNameView;
    private HolloutTextView aboutRequesterView;
    private HolloutTextView distanceToRequesterView;
    private TextView acceptRequestTextView;
    private TextView declineRequestView;

    private ParseObject signedInUser;
    private ParseObject requestOriginator;

    private Activity activity;
    private ParseObject feedObject;

    public ChatRequestView(Context context) {
        this(context, null);
    }

    public ChatRequestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRequestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.chat_request_item, this);
//        init();
    }

    public void bindData(final Activity activity, final ChatRequestsAdapter parent, final ParseObject feedObject) {
        this.activity = activity;
        this.signedInUser = AuthUtil.getCurrentUser();
        this.feedObject = feedObject;
        if (feedObject != null) {
            String requestType = feedObject.getString(AppConstants.FEED_TYPE);
            if (requestType.equals(AppConstants.FEED_TYPE_CHAT_REQUEST)) {
                requestOriginator = feedObject.getParseObject(AppConstants.FEED_CREATOR);
                if (requestOriginator != null) {
                    final String userDisplayName = requestOriginator.getString(AppConstants.APP_USER_DISPLAY_NAME);
                    if (StringUtils.isNotEmpty(userDisplayName) && !(activity instanceof UserProfileActivity)) {
                        requesterNameView.setText(WordUtils.capitalize(userDisplayName));
                    } else {
                        UiUtils.showView(aboutRequesterView, true);
                        UiUtils.showView(requesterNameView, false);
                        aboutRequesterView.setText("Hi, I sent you a chat request.");
                    }
                    String userProfilePhotoUrl = requestOriginator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                    if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                        UiUtils.loadImage(activity, userProfilePhotoUrl, requesterPhotoView);
                    } else {
                        requesterPhotoView.setImageResource(R.drawable.empty_profile);
                    }
                    if (!(activity instanceof UserProfileActivity)) {
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
                    } else {
                        UiUtils.showView(distanceToRequesterView, false);
                    }
                    acceptChatRequest(activity, parent, feedObject, userDisplayName);
                    declineChatRequest(activity, parent, feedObject, userDisplayName);
                }
            } else if (requestType.equals(AppConstants.FEED_TYPE_JOIN_GROUP_REQUEST)) {
//                ParseObject groupDetails = feedObject.getParseObject(AppConstants.GROUP_OR_ROOM);
                //TODO: Add Group support later.....Not now....We don't need such in a dating like app
            }
        }
    }

    private void declineChatRequest(final Activity activity, final ChatRequestsAdapter parent, final ParseObject feedObject, final String userDisplayName) {
        declineRequestView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                declineRequestView.setText(activity.getString(R.string.working));
//                ChatUtils.declineChatInvitation(requestOriginator.getString(AppConstants.REAL_OBJECT_ID), new DoneCallback<Boolean>() {
//                    @Override
//                    public void done(Boolean declined, Exception e) {
//                        if (e == null && declined) {
//                            ParseQuery<ParseObject> requestObjectQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
//                            requestObjectQuery.whereEqualTo(AppConstants.OBJECT_ID, feedObject.getObjectId());
//                            requestObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
//                                @Override
//                                public void done(final ParseObject object, ParseException e) {
//                                    if (e == null && object != null) {
//                                        object.deleteInBackground(new DeleteCallback() {
//                                            @Override
//                                            public void done(ParseException e) {
//                                                UiUtils.snackMessage("Request from " + userDisplayName + " declined successfully.", ChatRequestView.this, true, null, null);
//                                                if (parent != null) {
//                                                    removeRequest(parent, object);
//                                                }
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//                        }
//                    }
//                });
            }
        });
    }

    public void acceptChatRequest() {
        acceptRequestTextView.performClick();
    }

    private void acceptChatRequest(final Activity activity, final ChatRequestsAdapter parent, final ParseObject feedObject, final String userDisplayName) {
//        acceptRequestTextView.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                acceptRequestTextView.setText(activity.getString(R.string.working));
////                ChatUtils.acceptChatInvitation(requestOriginator.getString(AppConstants.REAL_OBJECT_ID).toLowerCase(), new DoneCallback<Boolean>() {
////                    @Override
////                    public void done(Boolean result, Exception e) {
////                        if (e == null) {
////                            //remove request
////                            ParseQuery<ParseObject> requestObjectQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
////                            requestObjectQuery.whereEqualTo(AppConstants.OBJECT_ID, feedObject.getObjectId());
////                            requestObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
////                                @Override
////                                public void done(final ParseObject returnedFeedObject, ParseException e) {
////
////                                    List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
////
////                                    if (signedInUserChats != null && !signedInUserChats.contains(requestOriginator.getString(AppConstants.REAL_OBJECT_ID).toLowerCase())) {
////                                        signedInUserChats.add(requestOriginator.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
////                                    }
////
////                                    if (signedInUserChats == null) {
////                                        signedInUserChats = new ArrayList<>();
////                                        signedInUserChats.add(requestOriginator.getString(AppConstants.REAL_OBJECT_ID));
////                                    }
////
////                                    signedInUser.put(AppConstants.APP_USER_CHATS, signedInUserChats);
////                                    HolloutPreferences.updateConversationTime(requestOriginator.getString(AppConstants.REAL_OBJECT_ID));
////                                    AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
////                                        @Override
////                                        public void done(Boolean result, Exception e) {
////                                            if (e == null) {
////                                                if (returnedFeedObject != null) {
////                                                    returnedFeedObject.deleteInBackground(new DeleteCallback() {
////                                                        @Override
////                                                        public void done(ParseException e) {
////                                                            if (e == null) {
////                                                                UiUtils.snackMessage("Request from " + userDisplayName + " successfully accepted.", ChatRequestView.this, true, null, null);
////                                                                if (parent != null) {
////                                                                    removeRequest(parent, feedObject);
////                                                                }
////                                                                EventBus.getDefault().post(AppConstants.REMOVE_SOMETHING);
////                                                                EventBus.getDefault().postSticky(AppConstants.REFRESH_CONVERSATIONS);
////                                                            } else {
////                                                                UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
////                                                            }
////                                                        }
////                                                    });
////                                                } else {
////                                                    UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
////                                                }
////                                            } else {
////                                                UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
////                                            }
////                                        }
////                                    });
////                                }
////                            });
////                        } else {
////                            UiUtils.snackMessage("Failed to accept request from " + userDisplayName + ". Please try again.", ChatRequestView.this, true, null, null);
////                        }
////                    }
////                });
////            }
////        });
//    }
//
//    private void removeRequest(ChatRequestsAdapter parent, ParseObject parseObject) {
//        parent.getChatRequests().remove(parseObject);
//        parent.notifyDataSetChanged();
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        ButterKnife.bind(this);
//    }
//
//    private void init() {
//        setOnClickListener(this);
//        setOnLongClickListener(this);
//        initViews();
//    }
//
//    private void initViews() {
//        requesterPhotoView = (CircleImageView) findViewById(R.id.requester_photo);
//        requesterNameView = (HolloutTextView) findViewById(R.id.requester_name);
//        aboutRequesterView = (HolloutTextView) findViewById(R.id.about_requester);
//        distanceToRequesterView = (HolloutTextView) findViewById(R.id.distance_to_requester);
//        acceptRequestTextView = (TextView) findViewById(R.id.accept_request);
//        declineRequestView = (TextView) findViewById(R.id.decline_request);
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (requestOriginator != null) {
//            Intent requesterInfoIntent = new Intent(activity, UserProfileActivity.class);
//            requesterInfoIntent.putExtra(AppConstants.PENDING_CHAT_REQUEST, feedObject);
//            activity.startActivity(requesterInfoIntent);
//        }
//    }
//
//    @Override
//    public boolean onLongClick(View v) {
//        return false;
//    }
//
//}
    }
}