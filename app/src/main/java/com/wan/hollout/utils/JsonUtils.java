package com.wan.hollout.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.sinch.gson.Gson;
import com.wan.hollout.models.ChatMessage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    public static ObjectMapper getMapper() {
        return objectMapper;
    }

    public static Gson getGson() {
        return new Gson();
    }

    public static Type getListType() {
        return new TypeToken<List<ChatMessage>>() {
        }.getType();
    }

}
