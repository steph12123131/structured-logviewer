package org.logviewer.ui;

import com.intellij.openapi.project.Project;
import lombok.Setter;
import org.logviewer.ScratchFileService;

import javax.swing.*;

@Setter
public class LogTagContextMenu extends JPopupMenu {

    private final JMenuItem openStack;
    private Object value;
    private Project project;

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        openStack.setVisible(this.stackTrace != null);
    }

    private String stackTrace;

    LogTagContextMenu() {
        JMenuItem openAsJson = new JMenuItem("Open As Json");
        JMenuItem openAsText = new JMenuItem("Open As Text");
        openStack = new JMenuItem("Open Stack");
        add(openAsJson);
        add(openAsText);
        add(openStack);
        openAsJson.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenJsonScratchFile("Scratch", value == null ? "" : value.toString());
        });
        openAsText.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenTextScratchFile("Scratch", value == null ? "" : value.toString());
        });
        openStack.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenTextScratchFile("Stack", stackTrace);
        });
    }


}
