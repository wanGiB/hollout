package com.wan.hollout.clients;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseObject;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.eventbuses.MessageReceivedEvent;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.MessageNotifier;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Wan Clem
 */

public class ChatClient {

    protected static final String TAG = "ChatClient";
    private ExecutorService executor = null;
    private static ChatClient instance;

    public static ChatClient getInstance() {
        if (instance == null) {
            instance = new ChatClient();
        }
        return instance;
    }

    private ChatClient() {
        this.executor = Executors.newCachedThreadPool();
    }

    public void startChatClient() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            listenForInBoundPrivateMessages(signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            listenForMessageDeliveryStatus(signedInUser.getString(AppConstants.REAL_OBJECT_ID));
        }
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    private void listenForInBoundPrivateMessages(final String userId) {
        FirebaseUtils
                .getUsersReference()
                .child(userId)
                .child(AppConstants.MESSAGES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    final ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                                    if (chatMessage != null) {
                                        HolloutLogger.d("NewUnreadMessage", chatMessage.toString());
                                        final String from = chatMessage.getFrom();
                                        //Mark Message as delivered
                                        chatMessage.setMessageStatus(MessageStatus.DELIVERED);
                                        chatMessage.setMessageDirection(MessageDirection.INCOMING);
                                        chatMessage.setConversationId(chatMessage.getFrom());
                                        chatMessage.setFromName(chatMessage.getFromName());
                                        DbUtils.createMessage(chatMessage);
                                        HolloutPreferences.incrementUnreadMessagesFrom(chatMessage.getFrom());
                                        incrementTotalUnreadChats(chatMessage);
                                        HolloutUtils.deserializeMessages(AppConstants.ALL_UNREAD_MESSAGES, new DoneCallback<List<ChatMessage>>() {
                                            @Override
                                            public void done(List<ChatMessage> result, Exception e) {
                                                List<ChatMessage> unreadMessages = (result != null && !result.isEmpty()) ? result : new ArrayList<ChatMessage>();
                                                unreadMessages.add(chatMessage);
                                                HolloutUtils.serializeMessages(unreadMessages, AppConstants.ALL_UNREAD_MESSAGES);
                                                HolloutPreferences.setTotalUnreadMessagesCount(unreadMessages.size());
                                                MessageNotifier.getInstance().notifyOnUnreadMessages();
                                                markMessageAsDelivered(chatMessage, from, userId);
                                            }
                                        });
                                        EventBus.getDefault().post(new MessageReceivedEvent(chatMessage));
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
    }

    private void incrementTotalUnreadChats(ChatMessage chatMessage) {
        Set<String> totalUnreadChats = HolloutPreferences.getTotalUnreadChats();
        if (HolloutUtils.isAContact(chatMessage.getFrom()) && !totalUnreadChats.contains(chatMessage.getFrom())) {
            totalUnreadChats.add(chatMessage.getFrom());
        }
        HolloutPreferences.saveTotalUnreadChats(totalUnreadChats);
    }

    private void markMessageAsDelivered(final ChatMessage chatMessage, String from, final String userId) {
        HashMap<String, Object> deliveryStatusProps = new HashMap<>();
        deliveryStatusProps.put(AppConstants.DELIVERY_STATUS, AppConstants.DELIVERED);
        FirebaseUtils.getMessageDeliveryStatus().child(from).child(chatMessage.getMessageId()).setValue(deliveryStatusProps).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseUtils.getUsersReference().child(userId).child(AppConstants.MESSAGES).child(chatMessage.getMessageId()).removeValue();
            }
        });
    }

    private void listenForMessageDeliveryStatus(String recipientId) {
        FirebaseUtils.getMessageDeliveryStatus().child(recipientId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    public void markMessagesAsRead(List<ChatMessage> messages) {
        for (final ChatMessage message : messages) {
            HashMap<String, Object> deliveryStatusProps = new HashMap<>();
            deliveryStatusProps.put(AppConstants.DELIVERY_STATUS, AppConstants.READ);
            FirebaseUtils.getMessageDeliveryStatus().child(message.getFrom()).child(message.getMessageId())
                    .setValue(deliveryStatusProps).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseUtils.getUsersReference().child(message.getFrom()).child(AppConstants.MESSAGES).child(message.getMessageId()).removeValue();
                }
            });
        }
    }

    public void sendMessage(final ChatMessage chatMessage) {
        DbUtils.createMessage(chatMessage);
        FirebaseUtils.getUsersReference()
                .child(chatMessage.getTo())
                .child(AppConstants.MESSAGES)
                .child(chatMessage.getMessageId())
                .setValue(chatMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Message Sent
                        chatMessage.setMessageStatus(MessageStatus.SENT);
                        DbUtils.updateMessage(chatMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error sending message
                chatMessage.setMessageStatus(MessageStatus.FAILED);
                DbUtils.updateMessage(chatMessage);
            }
        });
    }

    public ChatMessage getMessage(String messageId) {
        return DbUtils.getMessage(messageId);
    }

    @SuppressWarnings("ConstantConditions")
    public List<String> getBlackList() {
        return AuthUtil.getCurrentUser().getList(AppConstants.USER_BLACK_LIST);
    }

}
