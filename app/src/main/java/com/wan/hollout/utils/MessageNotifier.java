package com.wan.hollout.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
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
import android.support.v4.app.NotificationCompat;
import android.text.Spanned;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.interfaces.DoneCallback;
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author Wan Clem
 */
@SuppressWarnings("deprecation")
public class MessageNotifier {

    private final static String[] msgStandIns = {"&#x1f4f7; Photo", "&#x1f3a4; Voice Note",
            "&#x2316; Location", "&#x1f4f7; Video", "&#x1f3a4; Audio", "&#x260e; Contact", "sent a Document", "Sent a GIF", "Reaction"
    };

    private Context appContext;

    public static MessageNotifier getInstance() {
        return new MessageNotifier();
    }

    public MessageNotifier init(Context context) {
        appContext = context;
        return this;
    }

    public void notifyOnUnreadMessages() {
        HolloutUtils.deserializeMessages(AppConstants.ALL_UNREAD_MESSAGES, new DoneCallback<List<ChatMessage>>() {
            @Override
            public void done(List<ChatMessage> result, Exception e) {
                if (result != null && !result.isEmpty()) {
                    onNewMsg(result);
                }
            }
        });
    }

    private void onNewMsg(List<ChatMessage> chatMessages) {
        if (!chatMessages.isEmpty()) {
            if (chatMessages.size() == 1) {
                Intent userInfoIntent = new Intent(appContext, FetchUserInfoService.class);
                userInfoIntent.putExtra(AppConstants.EXTRA_USER_ID, chatMessages.get(0).getFrom());
                userInfoIntent.putExtra(AppConstants.UNREAD_MESSAGE, chatMessages.get(0));
                userInfoIntent.putExtra(AppConstants.NOTIFICATION_TYPE, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                appContext.startService(userInfoIntent);
            } else {
                if (fromSameSender(chatMessages)) {
                    Intent userInfoIntent = new Intent(appContext, FetchUserInfoService.class);
                    userInfoIntent.putExtra(AppConstants.EXTRA_USER_ID, chatMessages.get(0).getFrom());
                    userInfoIntent.putParcelableArrayListExtra(AppConstants.UNREAD_MESSAGES_FROM_SAME_SENDER, new ArrayList<>(chatMessages));
                    userInfoIntent.putExtra(AppConstants.NOTIFICATION_TYPE, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                    appContext.startService(userInfoIntent);
                } else {
                    sendMultipleSendersNotification(chatMessages);
                }
            }
        }
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
        } else {
            return msgStandIns[6];
        }
    }

    private boolean fromSameSender(List<ChatMessage> messages) {
        List<String> senders = new ArrayList<>();
        for (ChatMessage emMessage : messages) {
            String senderName = emMessage.getFromName();
            if (!senders.contains(senderName.trim().toLowerCase())) {
                senders.add(senderName.trim().toLowerCase());
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

    public void sendSingleNotification(final ChatMessage message, final ParseObject sender) {
        if (appContext == null) {
            appContext = ApplicationLoader.getInstance();
        }
        ChatClient.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent userProfileIntent = new Intent(appContext, ChatActivity.class);
                userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, sender);
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, userProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                String senderName = message.getFromName();
                String senderPhoto = message.getFromPhotoUrl();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
                builder.setContentTitle(WordUtils.capitalize(senderName));
                Spanned messageSpannable = UiUtils.fromHtml(getMessage(message));
                builder.setContentText(messageSpannable);
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLights(Color.parseColor("blue"), 500, 1000);

                Bitmap notificationInitiatorBitmap = BitmapFactory.decodeResource(appContext.getResources(), R.mipmap.ic_launcher);

                if (StringUtils.isNotEmpty(senderPhoto)) {
                    Resources res = appContext.getResources();
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
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    NotificationUtils.getNotificationManager().notify(AppConstants.CHAT_REQUEST_NOTIFICATION_ID, notification);
                }
            }
        });
    }

    public void sendSameSenderNotification(final List<ChatMessage> chatMessages, final ParseObject parseUser) {
        if (appContext == null) {
            appContext = ApplicationLoader.getInstance();
        }
        ChatClient.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Intent userProfileIntent = new Intent(appContext, ChatActivity.class);
                userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, userProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                String senderName = WordUtils.capitalize(parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
                String senderPhoto = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
                Spanned messageSpannable = UiUtils.fromHtml(HolloutPreferences.getTotalUnreadMessagesCount() + " new messages");
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                builder.setColor(Color.parseColor("#00628F"));
                Bitmap notificationInitiatorBitmap = BitmapFactory.decodeResource(appContext.getResources(), R.mipmap.ic_launcher);
                if (StringUtils.isNotEmpty(senderPhoto)) {
                    Resources res = appContext.getResources();
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
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    NotificationUtils.getNotificationManager().notify(AppConstants.CHAT_REQUEST_NOTIFICATION_ID, notification);
                }
            }
        });
    }

    private void sendMultipleSendersNotification(final List<ChatMessage> chatMessages) {
        if (appContext == null) {
            appContext = ApplicationLoader.getInstance();
        }
        ChatClient.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent mainIntent = new Intent(appContext, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
                Spanned messageSpannable = UiUtils.fromHtml(HolloutPreferences.getTotalUnreadMessagesCount() + " new messages");
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setAutoCancel(true);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                builder.setColor(Color.parseColor("#00628F"));
                Resources res = appContext.getResources();

                int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

                Bitmap notificationInitiatorBitmap =
                        getCircleBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(appContext.getResources(),
                                R.mipmap.ic_launcher),
                                width, height, false));

                builder.setContentTitle(WordUtils.capitalize(appContext.getString(R.string.app_name)))
                        .setLargeIcon(notificationInitiatorBitmap)
                        .setContentIntent(pendingIntent)
                        .setNumber(chatMessages.size())
                        .setStyle(inboxStyle)
                        .setSubText((chatMessages.size() == 1 ? "1 new message " : chatMessages.size() + " new messages") + " from " + ((getConversationIds(chatMessages).size() == 1) ? " 1 chat " : (getConversationIds(chatMessages).size() + " chats")));
                for (ChatMessage message : chatMessages) {
                    inboxStyle.addLine(UiUtils.fromHtml(message.getFromName() + ":" + getMessage(message)));
                }
                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    NotificationUtils.getNotificationManager().notify(AppConstants.CHAT_REQUEST_NOTIFICATION_ID, notification);
                }
            }
        });

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

}
