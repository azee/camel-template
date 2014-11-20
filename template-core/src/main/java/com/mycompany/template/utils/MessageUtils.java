package com.mycompany.template.utils;

import com.mycompany.template.beans.Message;

import java.util.HashMap;
import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;

/**
 * Created by azee on 20.11.14.
 */
public class MessageUtils {

    private static String POLL_ID_HEADER = "pollId";

    public static Map<String, Object> getHeaders(String pollId, Message message){
        return getHeaders(pollId, message.getClass());
    }

    public static Map<String, Object> getHeaders(String pollId, Class bodyClass){
        return getHeaders(pollId, bodyClass.getName());
    }

    public static Map<String, Object> getHeaders(String pollId, Object bodyClass){
        Map<String, Object> result = new HashMap<>();
        result.put(POLL_ID_HEADER, pollId);
        result.put(BODY_CLASS, bodyClass);
        return result;
    }
}
