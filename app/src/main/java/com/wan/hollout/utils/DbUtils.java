package com.wan.hollout.utils;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.CallLog;
import com.wan.hollout.models.HolloutEntity;
import com.wan.hollout.models.HolloutEntity_Table;
import com.wan.hollout.models.PathEntity;
import com.wan.hollout.models.PathEntity_Table;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Wan Clem
 */

@SuppressWarnings("WeakerAccess")
public class DbUtils {

    public static void getEntityName(int entityType, final String entityId, final DoneCallback<String> entityNameCallback) {
        String entityNameFromDb = getEntityName(entityId);
        if (StringUtils.isNotEmpty(entityNameFromDb)) {
            entityNameCallback.done(entityNameFromDb, null);
        }
        if (entityType == AppConstants.ENTITY_TYPE_INDIVIDUAL) {
            ParseQuery<ParseObject> parseUserParseQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            parseUserParseQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, entityId);
            parseUserParseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
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
            ParseQuery<ParseObject> groupsAndRoomsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            groupsAndRoomsQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, entityId);
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

            holloutEntity.entityName = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);

            holloutEntity.entityProfilePhotoUrl =  parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);

            holloutEntity.entityCoverPhotoUrl =  parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_COVER_PHOTO) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_COVER_PHOTO);

            holloutEntity.update();

        } else {

            HolloutEntity newHolloutEntity = new HolloutEntity();

            newHolloutEntity.entityId =  parseObject.getString(parseObject.getString(AppConstants.REAL_OBJECT_ID));

            newHolloutEntity.entityName =  parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);

            newHolloutEntity.entityProfilePhotoUrl =  parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);

            newHolloutEntity.entityCoverPhotoUrl =  parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_COVER_PHOTO) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_COVER_PHOTO);

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

    public static PathEntity getPathEntity(String pathName,String personId){
        return SQLite.select().from(PathEntity.class).where(PathEntity_Table.pathId.in(getPathId(personId,pathName))).querySingle();
    }

    public static void savePathEntity(String pathName,String personId){
        PathEntity pathEntity = SQLite.select().from(PathEntity.class).where(PathEntity_Table.pathId.in(getPathId(personId,pathName))).querySingle();
        if (pathEntity==null){
            PathEntity newPathEntity = new PathEntity();
            newPathEntity.pathId  = getPathId(personId,pathName);
            newPathEntity.personId = personId;
            newPathEntity.pathName = pathName;
            newPathEntity.save();
        }
    }

    public static String getPathId(String personId,String pathName){
        return personId+pathName;
    }

}