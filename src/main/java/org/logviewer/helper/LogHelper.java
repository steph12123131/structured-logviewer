package org.logviewer.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class LogHelper {

    public static Object getValueFromTag(Log log, LogTag tag) {

        Object result = switch (tag.getPathToString()) {
            case "level" -> log.getLevel();
            case "time" -> log.getTimestamp();
            case "message" -> log.getMessage();
            case "logger_name" -> log.getLoggerName();
            default -> null;
        };

        if (result == null) {
            Map<String, ?> current = log.getAdditional();

            for (String p : tag.getPath()) {
                result = current.get(p);
                if (result instanceof Map<?, ?> map) {
                    current = (Map<String, ?>) map;
                }
            }
        }
        return result;
    }

    public static Log decode(InputStream value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper.readTree(value);
        Log log = null;
        JsonNode payload = node.get("jsonPayload");
        if (payload != null) {

            try {
                log = mapper.treeToValue(payload, Log.class);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }

        }
        else {
            log=mapper.treeToValue(node, Log.class);
        }
        return log;
    }
}
