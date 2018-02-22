package com.wan.hollout.ui.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.utils.AppConstants;

import java.util.List;

/**
 * @author Wan Clem
 */

public class BatchMessageDeliveryStatusUpdater extends JobIntentService {

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            List<ChatMessage> messages = intent.getParcelableArrayListExtra(AppConstants.MESSAGES_FOR_BATCH_DELIVERY_UPDATE);
            if (messages != null && !messages.isEmpty()) {
                for (ChatMessage message : messages) {
                    ChatClient.getInstance().markMessageAsRead(message);
                }
            }
        }
    }

}
