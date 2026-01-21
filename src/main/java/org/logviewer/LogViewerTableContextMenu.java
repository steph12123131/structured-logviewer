package org.logviewer;

import com.intellij.openapi.project.Project;
import lombok.Setter;

import javax.swing.*;
@Setter
public class LogViewerTableContextMenu extends JPopupMenu {

    private Object value;
    private Project project;

    LogViewerTableContextMenu() {
        JMenuItem openAsJson = new JMenuItem("Open As Json");
        JMenuItem openAsText = new JMenuItem("Open As Text");
        add(openAsJson);
        add(openAsText);
        openAsJson.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenJsonScratchFile("Scratch",value.toString());
        });
        openAsText.addActionListener(actionEvent -> {
            ScratchFileService service = ScratchFileService.getInstance(project);
            service.createAndOpenTextScratchFile("Scratch",value.toString());
        });
    }


}
