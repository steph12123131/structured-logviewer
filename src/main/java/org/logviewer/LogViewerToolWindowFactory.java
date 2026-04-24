package org.logviewer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.logviewer.settings.LogViewerSettings;

public class LogViewerToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LogViewerSettings settings = LogViewerSettings.getInstance(project);
        LogViewer logViewer = new LogViewer(settings);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(logViewer, "Log Viewer", false);
        toolWindow.getContentManager().addContent(content);

        // Wiring ici : on sait que logViewer existe, on l'attache au ConsoleWatcher
        ConsoleWatcherService service = project.getService(ConsoleWatcherService.class);
        if (service != null) {
            service.getWatcher().addLogListener(logViewer);
        }
    }

}

