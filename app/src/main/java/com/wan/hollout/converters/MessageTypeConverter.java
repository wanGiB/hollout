package com.wan.hollout.converters;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.wan.hollout.enums.MessageType;

/**
 * @author Wan Clem
 */

public class MessageTypeConverter extends TypeConverter<String, MessageType> {

    @Override
    public String getDBValue(MessageType model) {
        return model.name();
    }

    @Override
    public MessageType getModelValue(String data) {
        return MessageType.valueOf(data);
    }

}