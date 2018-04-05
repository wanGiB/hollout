package com.wan.hollout.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spanned;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.services.FetchUserInfoService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author Wan Clem
 */
public class MessageNotifier {

    private final static String[] msgStandIns = {"&#x1f4f7; Photo", "&#x1f3a4; Voice Note",
            "&#x2316; Location", "&#x1f4f7; Video", "&#x1f3a4; Audio", "&#x260e; Contact", "sent a Document", "Sent a GIF", "Reaction", "Missed Call"
    };

    public static MessageNotifier getInstance() {
        return new MessageNotifier();
    }

    public void notifyOnUnreadMessages() {
        List<ChatMessage> allUnreadMessages = DbUtils.fetchAllUnreadMessages();
        if (!allUnreadMessages.isEmpty()) {
            HolloutLogger.d("HolloutNotifTag", "Yope, Messages found with size of " + allUnreadMessages.size());
            Collections.reverse(allUnreadMessages);
            onNewMsg(allUnreadMessages);
        } else {
            HolloutLogger.d("HolloutNotifTag", "Sorry, no messages o");
        }
    }

    private void onNewMsg(List<ChatMessage> chatMessages) {
        if (!chatMessages.isEmpty()) {
            if (chatMessages.size() == 1) {
                String messageFrom = chatMessages.get(0).getFrom();
                if (AppConstants.activeChatId != null && messageFrom.equals(AppConstants.activeChatId)) {
                    return;
                }
                HolloutLogger.d("HolloutNotifTag", "Active Chat is null ");
                FetchUserInfoService fetchUserInfoService = new FetchUserInfoService();
                Bundle userInfoIntent = new Bundle();
                userInfoIntent.putString(AppConstants.EXTRA_USER_ID, chatMessages.get(0).getFrom());
                userInfoIntent.putString(AppConstants.UNREAD_MESSAGE_ID, chatMessages.get(0).getMessageId());
                userInfoIntent.putString(AppConstants.NOTIFICATION_TYPE, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                fetchUserInfoService.onHandleWork(userInfoIntent);
            } else {
                if (fromSameSender(chatMessages)) {
                    String messageFrom = chatMessages.get(0).getFrom();
                    if (AppConstants.activeChatId != null && messageFrom.equals(AppConstants.activeChatId)) {
                        return;
                    }
                    HolloutLogger.d("HolloutNotifTag", "Active Chat is null ");
                    Bundle userInfoIntent = new Bundle();
                    userInfoIntent.putString(AppConstants.EXTRA_USER_ID, chatMessages.get(0).getFrom());
                    userInfoIntent.putSerializable(AppConstants.UNREAD_MESSAGES_FROM_SAME_SENDER, new ArrayList<>(chatMessages));
                    userInfoIntent.putString(AppConstants.NOTIFICATION_TYPE, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                    FetchUserInfoService fetchUserInfoService = new FetchUserInfoService();
                    fetchUserInfoService.onHandleWork(userInfoIntent);
                } else {
                    sendMultipleSendersNotification(chatMessages);
                }
            }
        }
    }


    public void sendSingleNotification(final ChatMessage message, final ParseObject sender) {

        ChatClient.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent userProfileIntent;
                if (HolloutUtils.isAContact(message.getFrom())) {
                    userProfileIntent = new Intent(ApplicationLoader.getInstance(), ChatActivity.class);
                    if (AppConstants.activeChatId == null) {
                        userProfileIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, true);
                    }
                    userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, sender);
                } else {
                    userProfileIntent = new Intent(ApplicationLoader.getInstance(), MainActivity.class);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.getInstance(), 0, userProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                String senderName = WordUtils.capitalize(message.getFromName());
                String senderPhoto = message.getFromPhotoUrl();

                String channelId = sender.getString(AppConstants.REAL_OBJECT_ID);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.getInstance(), channelId);
                NotificationHelper notificationHelper = new NotificationHelper(ApplicationLoader.getInstance().getApplicationContext(), channelId, senderName);

                builder.setContentTitle(WordUtils.capitalize(senderName));
                Spanned messageSpannable = UiUtils.fromHtml(getMessage(message));
                builder.setContentText(messageSpannable);
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLights(Color.parseColor("blue"), 500, 1000);

                Bitmap notificationInitiatorBitmap = BitmapFactory.decodeResource(ApplicationLoader.getInstance().getResources(), R.mipmap.ic_launcher);

                if (StringUtils.isNotEmpty(senderPhoto)) {
                    Resources res = ApplicationLoader.getInstance().getResources();
                    int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                    int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
                    Bitmap senderBitmap = getBitmapFromURL(senderPhoto);
                    if (senderBitmap != null) {
                        notificationInitiatorBitmap = getCircleBitmap(Bitmap.createScaledBitmap(senderBitmap, width, height, false));
                    }
                }

                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setLargeIcon(notificationInitiatorBitmap);
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                builder.setColor(Color.parseColor("#00628F"));

                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageSpannable)
                        .setBigContentTitle(WordUtils.capitalize(senderName)).setSummaryText("1 New Message"));

                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                if (HolloutUtils.canVibrate()) {
                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                }
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    notificationHelper.notify(AppConstants.NEW_MESSAGE_NOTIFICATION_ID, notification);
                }

            }

        });

    }

    public void sendSameSenderNotification(final List<ChatMessage> chatMessages, final ParseObject parseUser) {
        ChatClient.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Intent userProfileIntent;
                if (HolloutUtils.isAContact(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                    userProfileIntent = new Intent(ApplicationLoader.getInstance(), ChatActivity.class);
                    if (AppConstants.activeChatId == null) {
                        userProfileIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, true);
                    }
                    userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                } else {
                    userProfileIntent = new Intent(ApplicationLoader.getInstance(), MainActivity.class);
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.getInstance(), 0, userProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                String senderName = WordUtils.capitalize(parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
                String senderPhoto = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                String channelId = parseUser.getString(AppConstants.REAL_OBJECT_ID);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.getInstance(), channelId);
                NotificationHelper notificationHelper = new NotificationHelper(ApplicationLoader.getInstance().getApplicationContext(), channelId, senderName);

                Spanned messageSpannable = UiUtils.fromHtml(HolloutPreferences.getTotalUnreadMessagesCount() + " new messages");
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                builder.setColor(Color.parseColor("#00628F"));
                if (Build.VERSION.SDK_INT >= 26) {
                    builder.setContentText(messageSpannable);
                }
                Bitmap notificationInitiatorBitmap = BitmapFactory.decodeResource(ApplicationLoader.getInstance().getResources(), R.mipmap.ic_launcher);
                if (StringUtils.isNotEmpty(senderPhoto)) {
                    Resources res = ApplicationLoader.getInstance().getResources();
                    int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                    int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
                    Bitmap senderBitmap = getBitmapFromURL(senderPhoto);
                    if (senderBitmap != null) {
                        notificationInitiatorBitmap = getCircleBitmap(Bitmap.createScaledBitmap(senderBitmap, width, height, false));
                    }
                }
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setAutoCancel(true);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                builder.setContentTitle(WordUtils.capitalize(senderName))
                        .setLargeIcon(notificationInitiatorBitmap)
                        .setContentIntent(pendingIntent)
                        .setNumber(chatMessages.size())
                        .setStyle(inboxStyle)
                        .setSubText((chatMessages.size() == 1 ? "1 new message " : chatMessages.size() + " new messages"));
                for (ChatMessage message : chatMessages) {
                    inboxStyle.addLine(UiUtils.fromHtml(getMessage(message)));
                }
                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                if (HolloutUtils.canVibrate()) {
                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                }
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    notificationHelper.notify(AppConstants.NEW_MESSAGE_NOTIFICATION_ID, notification);
                }
            }
        });
    }

    private void sendMultipleSendersNotification(final List<ChatMessage> chatMessages) {
        ChatClient.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent mainIntent = new Intent(ApplicationLoader.getInstance(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.getInstance(), 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.getInstance(), getDefaultChannelId());
                NotificationHelper notificationHelper = new NotificationHelper(ApplicationLoader.getInstance().getApplicationContext(), getDefaultChannelId(), ApplicationLoader.getInstance().getString(R.string.app_name));

                Spanned messageSpannable = UiUtils.fromHtml(HolloutPreferences.getTotalUnreadMessagesCount() + " new messages");
                builder.setTicker(messageSpannable);
                if (Build.VERSION.SDK_INT >= 26) {
                    builder.setContentText((chatMessages.size() == 1 ? "1 new message " : chatMessages.size() + " new messages"));
                }
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setAutoCancel(true);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                builder.setColor(Color.parseColor("#00628F"));
                Resources res = ApplicationLoader.getInstance().getResources();

                int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

                Bitmap notificationInitiatorBitmap =
                        getCircleBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(ApplicationLoader.getInstance().getResources(),
                                R.mipmap.ic_launcher),
                                width, height, false));

                builder.setContentTitle(WordUtils.capitalize(ApplicationLoader.getInstance().getString(R.string.app_name)))
                        .setLargeIcon(notificationInitiatorBitmap)
                        .setContentIntent(pendingIntent)
                        .setNumber(chatMessages.size())
                        .setStyle(inboxStyle)
                        .setSubText((chatMessages.size() == 1 ? "1 new message " : chatMessages.size() + " new messages") + " from " + ((getConversationIds(chatMessages).size() == 1) ? " 1 chat " : (getConversationIds(chatMessages).size() + " chats")));

                for (ChatMessage message : chatMessages) {
                    if (HolloutUtils.isAContact(message.getFrom())) {
                        inboxStyle.addLine(WordUtils.capitalize(message.getFromName()) + ":" + UiUtils.fromHtml(getMessage(message)));
                    } else {
                        inboxStyle.addLine(WordUtils.capitalize(message.getFromName()) + " wants to chat with you");
                    }
                }
                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                if (HolloutUtils.canVibrate()) {
                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                }
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    notificationHelper.notify(AppConstants.NEW_MESSAGE_NOTIFICATION_ID, notification);
                }
            }
        });

    }

    private String getDefaultChannelId() {
        return ApplicationLoader.getInstance().getString(R.string.default_notification_channel_id);
    }

    private String getMissedCallsDefaultChannelId() {
        return ApplicationLoader.getInstance().getString(R.string.missed_calls_default_channel_id);
    }

    private static Bitmap getBitmapFromURL(final String strURL) {
        Callable<Bitmap> bitmapCallable = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    URL url = new URL(strURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        FutureTask<Bitmap> bitmapFutureTask = new FutureTask<>(bitmapCallable);
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(bitmapFutureTask);
        try {
            return bitmapFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public String getMessage(ChatMessage message) {
        MessageType messageType = message.getMessageType();
        if (messageType == MessageType.TXT) {
            return message.getMessageBody();
        } else if (messageType == MessageType.REACTION) {
            return msgStandIns[8];
        } else if (messageType == MessageType.GIF) {
            return msgStandIns[7];
        } else if (messageType == MessageType.IMAGE) {
            return msgStandIns[0];
        } else if (messageType == MessageType.VOICE) {
            return msgStandIns[1];
        } else if (messageType == MessageType.LOCATION) {
            return msgStandIns[2];
        } else if (messageType == MessageType.VIDEO) {
            long videoLength = message.getVideoDuration();
            return msgStandIns[3] + "<b> (" + UiUtils.getTimeString(videoLength) + ")</b>)";
        } else if (messageType == MessageType.AUDIO) {
            return msgStandIns[4];
        } else if (messageType == MessageType.CONTACT) {
            return msgStandIns[5];
        } else if (messageType == MessageType.CALL) {
            return message.getMessageBody();
        } else {
            return msgStandIns[6];
        }
    }

    private boolean fromSameSender(List<ChatMessage> messages) {
        List<String> senders = new ArrayList<>();
        for (ChatMessage emMessage : messages) {
            if (emMessage != null) {
                String senderId = emMessage.getFrom();
                if (!senders.contains(senderId.trim().toLowerCase())) {
                    senders.add(senderId.trim().toLowerCase());
                }
            }
        }
        return senders.size() == 1;
    }

    private List<String> getConversationIds(List<ChatMessage> messages) {
        List<String> conversationIds = new ArrayList<>();
        for (ChatMessage emMessage : messages) {
            String conversationId = emMessage.getFrom();
            if (!conversationIds.contains(conversationId)) {
                conversationIds.add(conversationId);
            }
        }
        return conversationIds;
    }

    void blowMissedCallsNotifications() {
        final List<ChatMessage> unreadMissedCalls = DbUtils.fetchAllUnseenMissedCalls();
        if (!unreadMissedCalls.isEmpty()) {
            ChatClient.getInstance().execute(new Runnable() {

                @Override
                public void run() {
                    Intent mainIntent = new Intent(ApplicationLoader.getInstance(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.getInstance(), 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.getInstance(), getMissedCallsDefaultChannelId());
                    NotificationHelper notificationHelper = new NotificationHelper(ApplicationLoader.getInstance().getApplicationContext(), getMissedCallsDefaultChannelId(), ApplicationLoader.getInstance().getString(R.string.app_name) + " Missed Calls");

                    Spanned messageSpannable = UiUtils.fromHtml(HolloutPreferences.getTotalUnreadMessagesCount() + "  missed calls");
                    builder.setTicker(messageSpannable);
                    if (Build.VERSION.SDK_INT >= 26) {
                        builder.setContentText((unreadMissedCalls.size() == 1 ? "1 missed call " : unreadMissedCalls.size() + " missed calls"));
                    }
                    builder.setSmallIcon(R.drawable.ic_call_missed_red_18dp);
                    builder.setLights(Color.parseColor("blue"), 500, 1000);
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    builder.setAutoCancel(true);
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    builder.setColor(Color.parseColor("#00628F"));
                    Resources res = ApplicationLoader.getInstance().getResources();

                    int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                    int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

                    Bitmap notificationInitiatorBitmap =
                            getCircleBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(ApplicationLoader.getInstance().getResources(),
                                    R.mipmap.ic_launcher),
                                    width, height, false));

                    builder.setContentTitle(WordUtils.capitalize(ApplicationLoader.getInstance().getString(R.string.app_name) + " Missed Calls"))
                            .setLargeIcon(notificationInitiatorBitmap)
                            .setContentIntent(pendingIntent)
                            .setNumber(unreadMissedCalls.size())
                            .setStyle(inboxStyle)
                            .setSubText((unreadMissedCalls.size() == 1 ? "1 missed call " : unreadMissedCalls.size() + " missed calls") + " from " + ((getConversationIds(unreadMissedCalls).size() == 1) ? "1 chat " : (getConversationIds(unreadMissedCalls).size() + " chats")));

                    for (ChatMessage message : unreadMissedCalls) {
                        if (message.getFromName() != null) {
                            if (HolloutUtils.isAContact(message.getFrom())) {
                                inboxStyle.addLine(WordUtils.capitalize(message.getFromName()) + ":" + UiUtils.fromHtml(getMessage(message)));
                            } else {
                                inboxStyle.addLine(WordUtils.capitalize(message.getFromName()) + " wants to chat with you");
                            }
                        }
                    }
                    Notification notification = builder.build();
                    notification.defaults |= Notification.DEFAULT_LIGHTS;
                    if (HolloutUtils.canVibrate()) {
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }
                    notification.defaults |= Notification.DEFAULT_SOUND;
                    if (pendingIntent != null) {
                        notificationHelper.notify(AppConstants.MISSED_CALLS_NOTIFICATIONS_ID, notification);
                    }
                }
            });
        }
    }

}
