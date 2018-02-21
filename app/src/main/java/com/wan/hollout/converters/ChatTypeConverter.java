package com.wan.hollout.converters;


import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.wan.hollout.enums.ChatType;

/**
 * @author Wan Clem
 */

public class ChatTypeConverter extends TypeConverter<String, ChatType> {

    @Override
    public String getDBValue(ChatType model) {
        return model.name();
    }

    @Override
    public ChatType getModelValue(String data) {
        return ChatType.valueOf(data);
    }

}
