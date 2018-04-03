package com.wan.hollout.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.sinch.gson.Gson;
import com.wan.hollout.models.ChatMessage;

import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static Gson getGSon() {
        return new Gson();
    }

    public static Type getListType() {
        return new TypeToken<List<ChatMessage>>() {
        }.getType();
    }

}
