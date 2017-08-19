package com.wan.hollout.utils;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.models.CallLog;
import com.wan.hollout.models.HolloutEntity;
import com.wan.hollout.models.HolloutEntity_Table;
import com.wan.hollout.models.MeetPoint;
import com.wan.hollout.models.MeetPoint_Table;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Wan Clem
 */

@SuppressWarnings("WeakerAccess")
public class DbUtils {

    public static String getMeetPoint(String senderId) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(senderId)).querySingle();
        if (meetPoint != null) {
            return meetPoint.getMeetPoint();
        } else {
            return null;
        }
    }

    public static void getEntityName(int entityType, final String entityId, final DoneCallback<String> entityNameCallback) {
        String entityNameFromDb = getEntityName(entityId);
        if (StringUtils.isNotEmpty(entityNameFromDb)) {
            entityNameCallback.done(entityNameFromDb, null);
        }
        if (entityType == AppConstants.ENTITY_TYPE_INDIVIDUAL) {
            ParseQuery<ParseUser> parseUserParseQuery = ParseUser.getQuery();
            parseUserParseQuery.whereEqualTo(AppConstants.APP_USER_ID, entityId);
            parseUserParseQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        if (object != null) {
                            String entityName = object.getString(AppConstants.APP_USER_DISPLAY_NAME);
                            if (StringUtils.isNotEmpty(entityName)) {
                                upsertEntity(entityId, object);
                                entityNameCallback.done(entityName, null);
                            } else {
                                entityNameCallback.done(null, null);
                            }
                        } else {
                            entityNameCallback.done(null, null);
                        }
                    } else {
                        entityNameCallback.done(null, e);
                    }
                }
            });
        } else if (entityType == AppConstants.ENTITY_TYPE_GROUP || entityType == AppConstants.ENTITY_TYPE_CHAT_ROOM) {
            ParseQuery<ParseObject> groupsAndRoomsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_AND_GROUPS);
            groupsAndRoomsQuery.whereEqualTo(AppConstants.GROUP_OR_CHAT_ROOM_ID, entityId);
            groupsAndRoomsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        if (object != null) {
                            String entityName = object.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);
                            if (StringUtils.isNotEmpty(entityName)) {
                                entityNameCallback.done(entityName, null);
                            } else {
                                entityNameCallback.done(null, null);
                            }
                        } else {
                            entityNameCallback.done(null, null);
                        }
                    } else {
                        entityNameCallback.done(null, e);
                    }
                }
            });
        }
    }

    private static void upsertEntity(String entityId, ParseObject parseObject) {
        HolloutEntity holloutEntity = SQLite.select().from(HolloutEntity.class).where(HolloutEntity_Table.entityId.eq(entityId)).querySingle();
        if (holloutEntity != null) {
            holloutEntity.entityName = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);
            holloutEntity.entityProfilePhotoUrl = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);
            holloutEntity.entityCoverPhotoUrl = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_COVER_PHOTO) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_COVER_PHOTO);
            holloutEntity.update();
        } else {
            HolloutEntity newHolloutEntity = new HolloutEntity();
            newHolloutEntity.entityId = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_ID) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_ID);
            newHolloutEntity.entityName = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);
            newHolloutEntity.entityProfilePhotoUrl = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);
            newHolloutEntity.entityCoverPhotoUrl = parseObject instanceof ParseUser ? parseObject.getString(AppConstants.APP_USER_COVER_PHOTO) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_COVER_PHOTO);
            newHolloutEntity.save();
        }
    }

    public static String getEntityName(String entityId) {
        HolloutEntity holloutEntity = SQLite.select().from(HolloutEntity.class).where(HolloutEntity_Table.entityId.eq(entityId)).querySingle();
        if (holloutEntity != null) {
            return holloutEntity.getEntityName();
        }
        return null;
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

    public static void createCallLog(String partyId, String partyName, String content, boolean incoming, boolean voiceCall) {
        CallLog callLog = new CallLog();
        callLog.content = content;
        callLog.partyId = partyId;
        callLog.callId = System.currentTimeMillis() + RandomStringUtils.random(5, true, true);
        callLog.partyName = partyName;
        callLog.incoming = incoming;
        callLog.voiceCall =voiceCall;
        callLog.save();
    }

}
