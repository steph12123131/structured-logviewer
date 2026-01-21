package org.logviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.logviewer.entity.Log;

import java.io.IOException;
import java.io.InputStream;

public class LogJsonHelper {

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
