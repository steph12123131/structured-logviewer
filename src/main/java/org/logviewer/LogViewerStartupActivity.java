package org.logviewer;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LogViewerStartupActivity implements ProjectActivity,StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        // À ce stade, toutes les ToolWindows sont initialisées
        ToolWindow logViewerToolWindow = getLogViewerToolWindow(project);

        initializeLogViewer(logViewerToolWindow, project);
    }

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        // À ce stade, toutes les ToolWindows sont initialisées
        ToolWindow logViewerToolWindow = getLogViewerToolWindow(project);
        ApplicationManager.getApplication().invokeLater(() -> {
            initializeLogViewer(logViewerToolWindow, project);
        });
        return null;
    }

    private static @Nullable ToolWindow getLogViewerToolWindow(@NotNull Project project) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return toolWindowManager.getToolWindow("logviewer-tool");
    }

    private void initializeLogViewer(ToolWindow toolWindow, Project project) {
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getContent(0); // Premier content

        if (content != null) {
            JComponent component = content.getComponent();
            if (component instanceof LogViewer viewer) {
                // S'enregistrer comme listener auprès du ConsoleWatcher
                ConsoleWatcherService service = project.getService(ConsoleWatcherService.class);
                if (service != null) {
                    service.getWatcher().addLogListener(viewer);
                }
            }
        }
    }

}