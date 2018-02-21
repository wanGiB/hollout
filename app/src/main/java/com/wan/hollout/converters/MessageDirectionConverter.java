package com.wan.hollout.converters;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.wan.hollout.enums.MessageDirection;

/**
 * @author Wan Clem
 */

public class MessageDirectionConverter extends TypeConverter<String, MessageDirection> {

    @Override
    public String getDBValue(MessageDirection model) {
        return model.name();
    }

    @Override
    public MessageDirection getModelValue(String data) {
        return MessageDirection.valueOf(data);
    }

}