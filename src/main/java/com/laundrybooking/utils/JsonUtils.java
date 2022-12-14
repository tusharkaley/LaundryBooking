package com.laundrybooking.utils;

import java.util.Map;

import org.json.JSONObject;

public final class JsonUtils {
    public static String buildJsonString(final Map<String, String> jsonKeyValuesToBuild) {
        final JSONObject jsonObject = new JSONObject(jsonKeyValuesToBuild);
        return jsonObject.toString();
    }
}
