package com.wan.hollout.utils;

import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.parse.ParseObject;
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
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ChatMessage_Table;
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

    public static PathEntity getPathEntity(String pathName, String personId) {
        return SQLite.select().from(PathEntity.class).where(PathEntity_Table.pathId.in(getPathId(personId, pathName))).querySingle();
    }

    public static void savePathEntity(String pathName, String personId) {
        PathEntity pathEntity = SQLite.select().from(PathEntity.class).where(PathEntity_Table.pathId.in(getPathId(personId, pathName))).querySingle();
        if (pathEntity == null) {
            PathEntity newPathEntity = new PathEntity();
            newPathEntity.setPathId(getPathId(personId, pathName));
            newPathEntity.setPersonId(personId);
            newPathEntity.setPathName(pathName);
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

    public static List<ChatMessage> fetchAllUnreadMessages() {
        return SQLite.select()
                .from(ChatMessage.class)
                .where(ChatMessage_Table.messageDirection.eq(MessageDirection.INCOMING))
                .and(ChatMessage_Table.messageStatus.notEq(MessageStatus.READ)).orderBy(ChatMessage_Table.timeStamp, false).queryList();
    }

    public static List<ChatMessage> fetchAllUnseenMissedCalls() {
        return SQLite.select()
                .from(ChatMessage.class)
                .where(ChatMessage_Table.messageDirection.eq(MessageDirection.OUTGOING))
                .and(ChatMessage_Table.messageType.eq(MessageType.CALL))
                .and(ChatMessage_Table.messageStatus.notEq(MessageStatus.READ))
                .orderBy(ChatMessage_Table.timeStamp, false).queryList();
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
                if (chatMessage.getMessageDirection() == MessageDirection.OUTGOING
                        && chatMessage.getMessageType() == MessageType.CALL
                        && chatMessage.getMessageStatus() != MessageStatus.READ) {
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
                            if (message.getMessageDirection() == MessageDirection.OUTGOING
                                    && message.getMessageType() == MessageType.CALL
                                    && message.getMessageStatus() != MessageStatus.READ) {
                                message.setMessageStatus(MessageStatus.READ);
                                FlowManager.getModelAdapter(ChatMessage.class).update(message);
                            }
                        }
                    }).processListener(new ProcessModelTransaction.OnModelProcessListener<ChatMessage>() {
                        @Override
                        public void onModelProcessed(long current, long total, ChatMessage modifiedModel) {
                            if (modifiedModel.getMessageDirection() == MessageDirection.OUTGOING
                                    && modifiedModel.getMessageType() == MessageType.CALL
                                    && modifiedModel.getMessageStatus() == MessageStatus.READ) {
                                return;
                            }
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

    public static void createNewMissedCallMessage(String callerName, String mCallerId,String message) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(mCallerId);
        chatMessage.setTo(mCallerId);
        chatMessage.setFromName(callerName);
        chatMessage.setMessageDirection(MessageDirection.OUTGOING);
        chatMessage.setMessageStatus(MessageStatus.SENT);
        chatMessage.setMessageId(System.currentTimeMillis() + RandomStringUtils.random(5, true, true));
        chatMessage.setMessageType(MessageType.CALL);
        chatMessage.setMessageBody(message);
        chatMessage.setTimeStamp(System.currentTimeMillis());
        if (signedInUser != null) {
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(signedInUserId)) {
                chatMessage.setFrom(signedInUserId);
            }
        }
        FlowManager.getModelAdapter(ChatMessage.class).save(chatMessage);
        MessageNotifier.getInstance().blowMissedCallsNotifications();
    }

}