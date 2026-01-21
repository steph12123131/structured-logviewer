package org.logviewer.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Log {

    @JsonProperty("@timestamp")
    private String timestamp;

    private String level;
    private String thread;

    // JSON: "logger_name"
    @JsonProperty("logger_name")
    private String loggerName;

    private String message;

    // Pour stocker tous les champs non mappés
    private Map<String, Object> additional = new HashMap<>();

    @JsonAnySetter
    public void addAdditional(String key, Object value) {
        additional.put(key, value);
    }

   public Optional<Object> getValue(List<String> path) {
        if (path.size()==1) {
            if (path.get(0).equals("@timestamp")) {
                return Optional.of(timestamp);
            }
            if (path.get(0).equals("level")) {
                return Optional.of(level);
            }
            if (path.get(0).equals("thread")) {
                return Optional.of(thread);
            }
            if (path.get(0).equals("logger_name")) {
                return Optional.of(loggerName);
            }
            if (path.get(0).equals("message")) {
                return Optional.of(message);
            }

        }
        Object value = null;
        Map<String, ?> current = additional;
        for (String p : path) {
            value = current.get(p);
            if (value instanceof Map<?, ?> map) {
                current = (Map<String, ?>) map;
            }

        }
        return Optional.ofNullable(value);
    }
}
