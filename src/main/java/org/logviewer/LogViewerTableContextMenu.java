package org.logviewer;

import com.intellij.openapi.project.Project;
import lombok.Setter;

import javax.swing.*;
@Setter
public class LogViewerTableContextMenu extends JPopupMenu {

    private final JMenuItem openStack;
    private Object value;
    private Project project;

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        openStack.setVisible(this.stackTrace!=null);
    }

    private String stackTrace;

    LogViewerTableContextMenu() {
        JMenuItem openAsJson = new JMenuItem("Open As Json");
        JMenuItem openAsText = new JMenuItem("Open As Text");
        openStack = new JMenuItem("Open Stack");
        add(openAsJson);
        add(openAsText);
        add(openStack);
        openAsJson.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenJsonScratchFile("Scratch",value.toString());
        });
        openAsText.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenTextScratchFile("Scratch",value.toString());
        });
        openStack.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenTextScratchFile("Stack",stackTrace);
        });
    }


}
