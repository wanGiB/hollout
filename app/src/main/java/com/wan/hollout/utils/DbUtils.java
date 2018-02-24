package com.wan.hollout.utils;

import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.database.HolloutDb;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.CallLog;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ChatMessage_Table;
import com.wan.hollout.models.HolloutUserEntity;
import com.wan.hollout.models.HolloutUserEntity_Table;
import com.wan.hollout.models.PathEntity;
import com.wan.hollout.models.PathEntity_Table;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
        HolloutUserEntity holloutUserEntity = SQLite.select().from(HolloutUserEntity.class).where(HolloutUserEntity_Table.entityId.eq(entityId)).querySingle();
        if (holloutUserEntity != null) {

            holloutUserEntity.entityName = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);

            holloutUserEntity.entityProfilePhotoUrl = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);

            holloutUserEntity.entityCoverPhotoUrl = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_COVER_PHOTO) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_COVER_PHOTO);

            holloutUserEntity.update();

        } else {

            HolloutUserEntity newHolloutUserEntity = new HolloutUserEntity();

            newHolloutUserEntity.entityId = parseObject.getString(parseObject.getString(AppConstants.REAL_OBJECT_ID));

            newHolloutUserEntity.entityName = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);

            newHolloutUserEntity.entityProfilePhotoUrl = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);

            newHolloutUserEntity.entityCoverPhotoUrl = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_COVER_PHOTO) : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_COVER_PHOTO);

            newHolloutUserEntity.save();

        }

    }

    public static String getEntityName(String entityId) {
        HolloutUserEntity holloutUserEntity = SQLite.select().from(HolloutUserEntity.class).where(HolloutUserEntity_Table.entityId.eq(entityId)).querySingle();
        if (holloutUserEntity != null) {
            return holloutUserEntity.getEntityName();
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
        callLog.voiceCall = voiceCall;
        callLog.save();
    }

    public static PathEntity getPathEntity(String pathName, String personId) {
        return SQLite.select().from(PathEntity.class).where(PathEntity_Table.pathId.in(getPathId(personId, pathName))).querySingle();
    }

    public static void savePathEntity(String pathName, String personId) {
        PathEntity pathEntity = SQLite.select().from(PathEntity.class).where(PathEntity_Table.pathId.in(getPathId(personId, pathName))).querySingle();
        if (pathEntity == null) {
            PathEntity newPathEntity = new PathEntity();
            newPathEntity.pathId = getPathId(personId, pathName);
            newPathEntity.personId = personId;
            newPathEntity.pathName = pathName;
            newPathEntity.save();
        }
    }

    public static String getPathId(String personId, String pathName) {
        return personId + pathName;
    }

    public static void createMessage(ChatMessage chatMessage) {
        FlowManager.getModelAdapter(ChatMessage.class).save(chatMessage);
    }

    public static void updateMessage(ChatMessage chatMessage) {
        FlowManager.getModelAdapter(ChatMessage.class).update(chatMessage);
    }

    public static ChatMessage getMessage(String messageId) {
        return SQLite.select().from(ChatMessage.class).where(ChatMessage_Table.messageId.eq(messageId)).querySingle();
    }

    public static void fetchMessagesInConversation(String conversationId, final DoneCallback<List<ChatMessage>> doneCallback) {
        SQLite.select()
                .from(ChatMessage.class)
                .where(ChatMessage_Table.conversationId.eq(conversationId))
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<ChatMessage>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<ChatMessage> tResult) {
                        doneCallback.done(tResult, null);
                        extractUnreadMessages(tResult);
                    }
                }).execute();
    }

    public static void fetchMoreMessagesInConversation(String conversationId, int offset, final DoneCallback<List<ChatMessage>> doneCallback) {
        try {
            SQLite.select()
                    .from(ChatMessage.class)
                    .where(ChatMessage_Table.conversationId.eq(conversationId))
                    .offset(offset)
                    .async()
                    .queryListResultCallback(new QueryTransaction.QueryResultListCallback<ChatMessage>() {
                        @Override
                        public void onListQueryResult(QueryTransaction transaction, @NonNull List<ChatMessage> tResult) {
                            doneCallback.done(tResult, null);
                            extractUnreadMessages(tResult);
                        }
                    }).execute();
        } catch (SQLiteException ignore) {
            doneCallback.done(new ArrayList<ChatMessage>(), null);
        }
    }

    private static void extractUnreadMessages(@NonNull List<ChatMessage> tResult) {
        List<ChatMessage> unreadMessagesList = new ArrayList<>();
        if (!tResult.isEmpty()) {
            for (ChatMessage chatMessage : tResult) {
                if (chatMessage.getMessageDirection() == MessageDirection.INCOMING && chatMessage.getMessageStatus() != MessageStatus.READ) {
                    if (!unreadMessagesList.contains(chatMessage)) {
                        unreadMessagesList.add(chatMessage);
                    }
                }
            }
        }
        if (!unreadMessagesList.isEmpty()) {
            checkAndMarkMessagesAsRead(unreadMessagesList);
        }
    }

    private static void checkAndMarkMessagesAsRead(List<ChatMessage> targetMessages) {
        if (!targetMessages.isEmpty()) {
            ProcessModelTransaction<ChatMessage> processModelTransaction =
                    new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<ChatMessage>() {
                        @Override
                        public void processModel(final ChatMessage message, DatabaseWrapper wrapper) {
                            if (message.getMessageDirection() == MessageDirection.INCOMING && message.getMessageStatus() != MessageStatus.READ) {
                                message.setMessageStatus(MessageStatus.READ);
                                FlowManager.getModelAdapter(ChatMessage.class).update(message);
                            }
                        }
                    }).processListener(new ProcessModelTransaction.OnModelProcessListener<ChatMessage>() {
                        @Override
                        public void onModelProcessed(long current, long total, ChatMessage modifiedModel) {
                            ChatClient.getInstance().markMessageAsRead(modifiedModel);
                        }
                    }).addAll(targetMessages).build();
            Transaction transaction = FlowManager
                    .getDatabase(HolloutDb.class)
                    .beginTransactionAsync(processModelTransaction)
                    .build();
            transaction.execute();
        }
    }

    public static ChatMessage getLastMessageInConversation(String conversationId) {
        return SQLite.select().from(ChatMessage.class).where(ChatMessage_Table.conversationId.eq(conversationId)).orderBy(ChatMessage_Table.timeStamp, false).querySingle();
    }

    public static void deleteConversation(String conversationId, final DoneCallback<Long[]> operationDoneCallback) {
        SQLite.select().from(ChatMessage.class)
                .where(ChatMessage_Table.conversationId.eq(conversationId))
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<ChatMessage>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<ChatMessage> tResult) {
                        if (!tResult.isEmpty()) {
                            performConversationDeletion(tResult, operationDoneCallback);
                        } else {
                            operationDoneCallback.done(new Long[]{-1L, 0L}, null);
                        }
                    }
                }).execute();
    }

    private static void performConversationDeletion(List<ChatMessage> messagesInConversation, final DoneCallback<Long[]> progressCallback) {
        ProcessModelTransaction<ChatMessage> processModelTransaction =
                new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<ChatMessage>() {
                    @Override
                    public void processModel(ChatMessage message, DatabaseWrapper wrapper) {
                        FlowManager.getModelAdapter(ChatMessage.class).delete(message);
                    }
                }).processListener(new ProcessModelTransaction.OnModelProcessListener<ChatMessage>() {
                    @Override
                    public void onModelProcessed(long current, long total, ChatMessage modifiedModel) {
                        if (progressCallback != null) {
                            progressCallback.done(new Long[]{current, total}, null);
                        }
                    }
                }).addAll(messagesInConversation).build();
        Transaction transaction = FlowManager
                .getDatabase(HolloutDb.class)
                .beginTransactionAsync(processModelTransaction)
                .build();
        transaction.execute();
    }

    public static void deleteMessage(ChatMessage message) {
        FlowManager.getModelAdapter(ChatMessage.class).delete(message);
    }

    public static void searchMessages(String searchString, final DoneCallback<List<ChatMessage>> messageSearchDoneCallBack) {
        SQLite.select().from(ChatMessage.class)
                .where(ChatMessage_Table.messageBody.like("%" + searchString + "%"))
                .or(ChatMessage_Table.fileCaption.like("%" + searchString + "%"))
                .or(ChatMessage_Table.documentName.like("%" + searchString + "%"))
                .or(ChatMessage_Table.locationAddress.like("%" + searchString + "%"))
                .or(ChatMessage_Table.contactName.like("%" + searchString + "%"))
                .or(ChatMessage_Table.contactNumber.like("%" + searchString + "%"))
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<ChatMessage>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull final List<ChatMessage> queriedWords) {
                        messageSearchDoneCallBack.done(queriedWords, null);
                    }
                }).execute();
    }

}