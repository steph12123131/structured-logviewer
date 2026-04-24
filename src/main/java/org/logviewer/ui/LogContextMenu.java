package org.logviewer.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import lombok.Setter;
import org.logviewer.ScratchFileService;
import org.logviewer.entity.Log;

import javax.swing.*;
import java.util.Map;
import java.util.TreeMap;

@Setter
public class LogContextMenu extends JPopupMenu {

    private final JMenuItem openStack;
    private Log log;
    private ObjectMapper mapper = new ObjectMapper();

    public void setLog(Log log) {
        this.log = log;
        openStack.setVisible(this.log.getStackTrace() != null);
    }

    LogContextMenu() {
        JMenuItem openAsJson = new JMenuItem("Open As Json");
        openStack = new JMenuItem("Open Stack");
        add(openAsJson);

        add(openStack);


        openAsJson.addActionListener(actionEvent -> {

            String value = null;
            try {
                Map<String,Object> map = new TreeMap<>();
                map.put("timestamp",log.getTimestamp());
                map.put("stack_trace",log.getStackTrace());
                map.put("level",log.getLevel());
                map.put("message",log.getMessage());
                map.put("logger_name",log.getLoggerName());
                map.put("thread",log.getThread());
                map.putAll(log.getAdditional());
                value = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
            } catch (JsonProcessingException e) {
                value = "";
            }
            ScratchFileService service = ScratchFileService.getInstance(log.getProject());
            service.createAndOpenJsonScratchFile("Scratch-" + log.getTimestamp(), value);
        });
        openStack.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(log.getProject());
            service.createAndOpenTextScratchFile("Stack-" + log.getTimestamp(), log.getStackTrace());
        });

    }


}
