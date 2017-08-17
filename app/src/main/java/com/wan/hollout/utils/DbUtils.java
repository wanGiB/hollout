package com.wan.hollout.utils;

import com.parse.ParseObject;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.wan.hollout.models.HolloutConversation;
import com.wan.hollout.models.HolloutConversation_Table;
import com.wan.hollout.models.MeetPoint;
import com.wan.hollout.models.MeetPoint_Table;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Wan Clem
 */

public class DbUtils {

    public static String getMeetPoint(String senderId) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(senderId)).querySingle();
        if (meetPoint != null) {
            return meetPoint.getMeetPoint();
        } else {
            return null;
        }
    }

    public static void addToMeetPoints(String recipientId, String meetPointString) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(recipientId)).querySingle();
        if (meetPoint == null) {
            MeetPoint newMeetPoint = new MeetPoint();
            newMeetPoint.senderId = recipientId;
            newMeetPoint.meetPoint = meetPointString;
            newMeetPoint.save();
        }
    }

    public static void deleteMeetPointWithUser(String userId) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(userId)).querySingle();
        if (meetPoint != null) {
            meetPoint.delete();
        }
    }

    public static void upsertConversation(ParseObject parseObject, ParseObject lastMessageObject) {
        HolloutConversation holloutConversation = SQLite.select().from(HolloutConversation.class)
                .where(HolloutConversation_Table.conversationId.eq(parseObject.getObjectId())).querySingle();
        if (holloutConversation != null) {
            String objectDisplayName = parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME);
            if (StringUtils.isNotEmpty(objectDisplayName)) {
                holloutConversation.conversationName = objectDisplayName;
            }
            String objectPhotoUrl = parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(objectPhotoUrl)) {
                holloutConversation.conversationPhoto = objectPhotoUrl;
            }
            String objectOnlineStatus = parseObject.getString(AppConstants.APP_USER_ONLINE_STATUS);
            holloutConversation.online = StringUtils.isNotEmpty(objectOnlineStatus) && objectOnlineStatus.equals(AppConstants.ONLINE);
            String userStatus = parseObject.getString(AppConstants.APP_USER_STATUS);
            if (StringUtils.isNotEmpty(userStatus)) {
                holloutConversation.lastMessage = userStatus;
            }
            if (lastMessageObject != null) {

            }
            holloutConversation.update();
        }
    }

    private static void createNewConversation(ParseObject parseObject) {

    }

}
