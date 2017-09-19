package com.wan.hollout.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */
class EMMessageSerializer extends Serializer<EMMessage>{

    @Override
    public void write(Kryo kryo, Output output, EMMessage object) {
        output.writeString(object.getMsgId());
    }

    @Override
    public EMMessage read(Kryo kryo, Input input, Class<EMMessage> type) {
        return EMClient.getInstance().chatManager().getMessage(input.readString());
    }

}
