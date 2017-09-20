package com.wan.hollout.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.services.FetchUserInfoService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

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
public class MessageNotifier {

    private final static String[] msgStandIns = {"Photo", "Voice Note",
            "Location", "Video", "Audio", "Contact", "Document", "GIF", "Reaction"
    };
    private Context appContext;

    public static MessageNotifier getInstance() {
        return new MessageNotifier();
    }

    public MessageNotifier init(Context context) {
        appContext = context;
        return this;
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void onNewMsg(List<EMMessage> emMessages) {
        if (!emMessages.isEmpty()) {
            if (emMessages.size() == 1) {
                Intent userInfoIntent = new Intent(appContext, FetchUserInfoService.class);
                userInfoIntent.putExtra(AppConstants.EXTRA_USER_ID, emMessages.get(0).getFrom());
                userInfoIntent.putExtra(AppConstants.UNREAD_MESSAGE, emMessages.get(0));
                userInfoIntent.putExtra(AppConstants.NOTIFICATION_TYPE, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                appContext.startService(userInfoIntent);
            } else {
                if (fromSameSender(emMessages)) {
                    Intent userInfoIntent = new Intent(appContext, FetchUserInfoService.class);
                    userInfoIntent.putExtra(AppConstants.EXTRA_USER_ID, emMessages.get(0).getFrom());
                    userInfoIntent.putParcelableArrayListExtra(AppConstants.UNREAD_MESSAGES_FROM_SAME_SENDER, new ArrayList<>(emMessages));
                    userInfoIntent.putExtra(AppConstants.NOTIFICATION_TYPE, AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE);
                    appContext.startService(userInfoIntent);
                } else {
                    sendMultipleSendersNotification(emMessages);
                }
            }
        }
    }

    public String getMessage(EMMessage message) {
        if (message.getType() == EMMessage.Type.TXT) {
            try {
                String messageAttributeType = message.getStringAttribute(AppConstants.MESSAGE_ATTR_TYPE);
                if (messageAttributeType != null) {
                    switch (messageAttributeType) {
                        case AppConstants.MESSAGE_ATTR_TYPE_REACTION:
                            return msgStandIns[8];
                        case AppConstants.MESSAGE_ATTR_TYPE_GIF:
                            return msgStandIns[7];
                    }
                } else {
                    return ((EMTextMessageBody) message.getBody()).getMessage();
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
                return ((EMTextMessageBody) message.getBody()).getMessage();
            }
            return ((EMTextMessageBody) message.getBody()).getMessage();
        } else if (message.getType() == EMMessage.Type.IMAGE) {
            return msgStandIns[0];
        } else if (message.getType() == EMMessage.Type.VOICE) {
            return msgStandIns[1];
        } else if (message.getType() == EMMessage.Type.LOCATION) {
            return msgStandIns[2];
        } else if (message.getType() == EMMessage.Type.VIDEO) {
            return msgStandIns[3];
        } else if (message.getType() == EMMessage.Type.FILE) {
            try {
                String fileType = message.getStringAttribute(AppConstants.FILE_TYPE);
                switch (fileType) {
                    case AppConstants.FILE_TYPE_AUDIO:
                        return msgStandIns[4];
                    case AppConstants.FILE_TYPE_CONTACT:
                        return msgStandIns[5];
                    default:
                        return msgStandIns[6];
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        return "New Message";
    }


    private boolean fromSameSender(List<EMMessage> messages) {
        List<String> senders = new ArrayList<>();
        for (EMMessage emMessage : messages) {
            try {
                String senderName = emMessage.getStringAttribute(AppConstants.APP_USER_DISPLAY_NAME);
                if (!senders.contains(senderName.trim().toLowerCase())) {
                    senders.add(senderName.trim().toLowerCase());
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        return senders.size() == 1;
    }

    private List<String> getConversationIds(List<EMMessage> messages) {
        List<String> conversationIds = new ArrayList<>();
        for (EMMessage emMessage : messages) {
            String conversationId = emMessage.getFrom();
            if (!conversationIds.contains(conversationId)) {
                conversationIds.add(conversationId);
            }
        }
        return conversationIds;
    }

    public void sendSingleNotification(final EMMessage message, final ParseObject sender) {
        if (appContext == null) {
            appContext = ApplicationLoader.getInstance();
        }
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent userProfileIntent = new Intent(appContext, ChatActivity.class);
                userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, sender);
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, userProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                String senderName = appContext.getString(R.string.app_name);
                try {
                    senderName = message.getStringAttribute(AppConstants.APP_USER_DISPLAY_NAME);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                String senderPhoto = null;
                try {
                    senderPhoto = message.getStringAttribute(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
                builder.setContentTitle(senderName);
                Spanned messageSpannable = UiUtils.fromHtml(getMessage(message));
                builder.setContentText(messageSpannable);
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                Bitmap notificationInitiatorBitmap = getCircleBitmap(BitmapFactory.decodeResource(appContext.getResources(), R.mipmap.ic_launcher));
                if (StringUtils.isNotEmpty(senderPhoto)) {
                    notificationInitiatorBitmap = getCircleBitmap(getBitmapFromURL(senderPhoto));
                }
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setLargeIcon(notificationInitiatorBitmap);
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                builder.setContentText(messageSpannable);
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(messageSpannable).setBigContentTitle(senderName));
                builder.setSubText("1 New Message");
                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    getNotificationManager(appContext).notify(AppConstants.CHAT_REQUEST_NOTIFICATION_ID, notification);
                }
            }
        });
    }

    public void sendSameSenderNotification(final List<EMMessage> emMessages, final ParseObject parseUser) {
        if (appContext == null) {
            appContext = ApplicationLoader.getInstance();
        }
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent userProfileIntent = new Intent(appContext, ChatActivity.class);
                userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, userProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                String senderName = WordUtils.capitalize(parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
                String senderPhoto = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
                Spanned messageSpannable = UiUtils.fromHtml(EMClient.getInstance().chatManager().getUnreadMessageCount() + " new messages");
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                Bitmap notificationInitiatorBitmap = getCircleBitmap(BitmapFactory.decodeResource(appContext.getResources(), R.mipmap.ic_launcher));
                if (StringUtils.isNotEmpty(senderPhoto)) {
                    notificationInitiatorBitmap = getCircleBitmap(getBitmapFromURL(senderPhoto));
                }
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setAutoCancel(true);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                builder.setContentTitle(WordUtils.capitalize(senderName))
                        .setLargeIcon(notificationInitiatorBitmap)
                        .setContentIntent(pendingIntent)
                        .setNumber(emMessages.size())
                        .setStyle(inboxStyle)
                        .setSubText((emMessages.size() == 1 ? "1 new message " : emMessages.size() + " new messages"));

                for (EMMessage message : emMessages) {
                    inboxStyle.addLine(UiUtils.fromHtml(getMessage(message)));
                }
                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    getNotificationManager(appContext).notify(AppConstants.CHAT_REQUEST_NOTIFICATION_ID, notification);
                }
            }
        });
    }

    private void sendMultipleSendersNotification(final List<EMMessage> emMessages) {
        if (appContext == null) {
            appContext = ApplicationLoader.getInstance();
        }
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                Intent mainIntent = new Intent(appContext, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
                Spanned messageSpannable = UiUtils.fromHtml(EMClient.getInstance().chatManager().getUnreadMessageCount() + " new messages");
                builder.setTicker(messageSpannable);
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setLights(Color.parseColor("blue"), 500, 1000);
                Bitmap notificationInitiatorBitmap = getCircleBitmap(BitmapFactory.decodeResource(appContext.getResources(), R.mipmap.ic_launcher));
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setAutoCancel(true);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                builder.setContentTitle(WordUtils.capitalize(appContext.getString(R.string.app_name)))
                        .setLargeIcon(notificationInitiatorBitmap)
                        .setContentIntent(pendingIntent)
                        .setNumber(emMessages.size())
                        .setStyle(inboxStyle)
                        .setSubText((emMessages.size() == 1 ? "1 new message " : emMessages.size() + " new messages") + " from " + ((getConversationIds(emMessages).size() == 1) ? " 1 chat " : (getConversationIds(emMessages).size() + " chats")));
                for (EMMessage message : emMessages) {
                    try {
                        inboxStyle.addLine(UiUtils.fromHtml(message.getStringAttribute(AppConstants.APP_USER_DISPLAY_NAME) + ":" + getMessage(message)));
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
                Notification notification = builder.build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;
                if (pendingIntent != null) {
                    getNotificationManager(appContext).notify(AppConstants.CHAT_REQUEST_NOTIFICATION_ID, notification);
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

    private Bitmap getCircleBitmap(Bitmap bitmap){
        Bitmap output=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = Color.RED;
        Paint paint = new Paint();
        Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);
        canvas.drawOval(rectF,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        bitmap.recycle();
        return output;
    }

}
