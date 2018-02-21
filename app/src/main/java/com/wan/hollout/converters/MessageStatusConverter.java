package com.wan.hollout.converters;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.wan.hollout.enums.MessageStatus;

/**
 * @author Wan Clem
 */

public class MessageStatusConverter extends TypeConverter<String, MessageStatus> {

    @Override
    public String getDBValue(MessageStatus model) {
        return model.name();
    }

    @Override
    public MessageStatus getModelValue(String data) {
        return MessageStatus.valueOf(data);
    }

}