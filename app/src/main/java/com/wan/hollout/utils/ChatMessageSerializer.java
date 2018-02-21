package com.wan.hollout.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.wan.hollout.models.ChatMessage;

/**
 * @author Wan Clem
 */

public class ChatMessageSerializer extends Serializer<ChatMessage> {

    @Override
    public void write(Kryo kryo, Output output, ChatMessage object) {
        output.writeString(object.getMessageId());
    }

    @Override
    public ChatMessage read(Kryo kryo, Input input, Class<ChatMessage> type) {
        return DbUtils.getMessage(input.readString());
    }

}
