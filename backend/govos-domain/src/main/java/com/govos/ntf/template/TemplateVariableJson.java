package com.govos.ntf.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serializes declared template variable names to/from JSON stored on {@code NotificationTemplate}.
 */
public final class TemplateVariableJson {

    private static final Pattern JSON_STRING = Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");

    private TemplateVariableJson() {
    }

    public static String toJson(List<String> variables) {
        if (variables == null || variables.isEmpty()) {
            return null;
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < variables.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append('"').append(escape(variables.get(i))).append('"');
        }
        return json.append(']').toString();
    }

    public static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException("templateVariables must be a JSON array of strings");
        }
        List<String> result = new ArrayList<>();
        Matcher matcher = JSON_STRING.matcher(trimmed);
        while (matcher.find()) {
            result.add(unescape(matcher.group(1)));
        }
        return result;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
