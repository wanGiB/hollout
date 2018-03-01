package com.wan.hollout.clients;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseObject;
import com.wan.hollout.api.JsonApiClient;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.eventbuses.MessageReceivedEvent;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.MessageNotifier;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unused")
public class ChatClient {

    protected static final String TAG = "ChatClient";
    private ExecutorService executor = null;
    private static ChatClient instance;
    private boolean isStarted = false;

    private ValueEventListener messageDeliveryStatusValueEventListener, inBoundMessagesValueEventListener;

    public static ChatClient getInstance() {
        if (instance == null) {
            instance = new ChatClient();
        }
        return instance;
    }

    private ChatClient() {
        this.executor = Executors.newCachedThreadPool();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void startChatClient() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            listenForInBoundPrivateMessages(signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            listenForMessageDeliveryStatus(signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            isStarted = true;
        }
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    private void listenForInBoundPrivateMessages(final String userId) {
        DatabaseReference inBoundMessagesDatabaseReference = FirebaseUtils
                .getUsersReference()
                .child(userId)
                .child(AppConstants.MESSAGES);
        if (inBoundMessagesValueEventListener != null) {
            inBoundMessagesDatabaseReference.removeEventListener(inBoundMessagesValueEventListener);
        }
        inBoundMessagesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                execute(new Runnable() {
                    @Override
                    public void run() {
                        if (dataSnapshot != null) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    final ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                                    if (chatMessage != null) {
                                        final String from = chatMessage.getFrom();
                                        //Mark Message as delivered
                                        chatMessage.setMessageStatus(MessageStatus.DELIVERED);
                                        chatMessage.setMessageDirection(MessageDirection.INCOMING);
                                        chatMessage.setConversationId(chatMessage.getFrom());
                                        chatMessage.setFromName(chatMessage.getFromName());
                                        DbUtils.createMessage(chatMessage);
                                        markMessageAsDelivered(chatMessage, from, userId);
                                        HolloutPreferences.incrementUnreadMessagesFrom(chatMessage.getFrom());
                                        incrementTotalUnreadChats(chatMessage);
                                        List<ChatMessage> allUnreadMessages = DbUtils.fetchAllUnreadMessages();
                                        HolloutPreferences.setTotalUnreadMessagesCount(allUnreadMessages.size());
                                        MessageNotifier.getInstance().notifyOnUnreadMessages();
                                        EventBus.getDefault().post(new MessageReceivedEvent(chatMessage));
                                    }
                                }
                            }
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        inBoundMessagesDatabaseReference.addValueEventListener(inBoundMessagesValueEventListener);
    }

    private void listenForMessageDeliveryStatus(final String signedInUserId) {
        DatabaseReference messageDeliveryStatusDatabaseReference = FirebaseUtils.getMessageDeliveryStatus().child(signedInUserId);
        if (messageDeliveryStatusValueEventListener != null) {
            messageDeliveryStatusDatabaseReference.removeEventListener(messageDeliveryStatusValueEventListener);
        }
        messageDeliveryStatusValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                execute(new Runnable() {
                    @Override
                    public void run() {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                };
                                Map<String, Object> deliveryStatusMap = snapshot.getValue(genericTypeIndicator);
                                if (deliveryStatusMap != null) {
                                    HolloutLogger.d("ReceivedMsgProps", "Props aren't null with value = " + deliveryStatusMap.toString());
                                    String deliveryStatus = (String) deliveryStatusMap.get(AppConstants.DELIVERY_STATUS);
                                    if (deliveryStatus != null) {
                                        ChatMessage message = DbUtils.getMessage(snapshot.getKey());
                                        if (message != null) {
                                            if (deliveryStatus.equals(AppConstants.DELIVERED)) {
                                                message.setMessageStatus(MessageStatus.DELIVERED);
                                            } else if (deliveryStatus.equals(AppConstants.READ)) {
                                                message.setMessageStatus(MessageStatus.READ);
                                                FirebaseUtils.getMessageDeliveryStatus().child(signedInUserId).child(snapshot.getKey()).removeValue();
                                            }
                                            DbUtils.updateMessage(message);
                                        } else {
                                            FirebaseUtils.getMessageDeliveryStatus().child(signedInUserId).child(snapshot.getKey()).removeValue();
                                        }
                                    }
                                } else {
                                    HolloutLogger.d("ReceivedMsgProps", "Props are null");
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };
        messageDeliveryStatusDatabaseReference.addValueEventListener(messageDeliveryStatusValueEventListener);
    }

    private void incrementTotalUnreadChats(ChatMessage chatMessage) {
        Set<String> totalUnreadChats = HolloutPreferences.getTotalUnreadChats();
        if (HolloutUtils.isAContact(chatMessage.getFrom()) && !totalUnreadChats.contains(chatMessage.getFrom())) {
            totalUnreadChats.add(chatMessage.getFrom());
        }
        HolloutPreferences.saveTotalUnreadChats(totalUnreadChats);
    }

    private void markMessageAsDelivered(final ChatMessage chatMessage, final String from, final String userId) {
        execute(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> deliveryStatusProps = new HashMap<>();
                deliveryStatusProps.put(AppConstants.DELIVERY_STATUS, AppConstants.DELIVERED);
                FirebaseUtils.getMessageDeliveryStatus().child(from).child(chatMessage.getMessageId()).setValue(deliveryStatusProps).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseUtils.getUsersReference().child(userId).child(AppConstants.MESSAGES).child(chatMessage.getMessageId()).removeValue();
                    }
                });
            }
        });
    }

    public void markMessageAsRead(final ChatMessage message) {
        execute(new Runnable() {
            @Override
            public void run() {
                if (message.getMessageDirection() == MessageDirection.INCOMING && message.getMessageStatus() == MessageStatus.READ) {
                    final HashMap<String, Object> deliveryStatusProps = new HashMap<>();
                    deliveryStatusProps.put(AppConstants.DELIVERY_STATUS, AppConstants.READ);
                    FirebaseUtils.getMessageDeliveryStatus().child(message.getFrom()).child(message.getMessageId())
                            .setValue(deliveryStatusProps);
                }
            }
        });
    }

    public void sendMessage(final ChatMessage chatMessage, final ParseObject recipientProperties) {
        DbUtils.createMessage(chatMessage);
        FirebaseUtils.getUsersReference()
                .child(chatMessage.getTo())
                .child(AppConstants.MESSAGES)
                .child(chatMessage.getMessageId())
                .setValue(chatMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        execute(new Runnable() {
                            @Override
                            public void run() {
                                //Message Sent
                                chatMessage.setMessageStatus(MessageStatus.SENT);
                                DbUtils.updateMessage(chatMessage);
                                String recipientOnlineStatus = recipientProperties.getString(AppConstants.APP_USER_ONLINE_STATUS);
                                String recipientToken = recipientProperties.getString(AppConstants.USER_FIREBASE_TOKEN);
                                if (recipientOnlineStatus != null && !recipientOnlineStatus.equals(AppConstants.ONLINE)) {
                                    if (StringUtils.isNotEmpty(recipientToken)) {
                                        JsonApiClient.sendFirebasePushNotification(recipientToken, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                                    }
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                execute(new Runnable() {
                    @Override
                    public void run() {
                        //Error sending message
                        chatMessage.setMessageStatus(MessageStatus.FAILED);
                        DbUtils.updateMessage(chatMessage);
                    }
                });

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
