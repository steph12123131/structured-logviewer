package org.logviewer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogViewerToolWindowFactory implements ToolWindowFactory {

    public LogViewer getLogViewer(Project project) {
        if (!logViewers.containsKey(project.getProjectFilePath())) {
            LogViewerSettings settings = LogViewerSettings.getInstance(project);
            LogViewer logViewer = new LogViewer(settings);
            logViewers.put(project.getProjectFilePath(),logViewer);
        }
        return logViewers.get(project.getProjectFilePath());
    }

    private Map<String,LogViewer> logViewers=new HashMap<>();


    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(getLogViewer(project), "Log Viewer", false);
        toolWindow.getContentManager().addContent(content);
    }

}
