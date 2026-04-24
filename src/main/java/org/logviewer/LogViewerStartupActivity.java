package org.logviewer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * No-op : le wiring LogViewer ↔ ConsoleWatcher est fait dans LogViewerToolWindowFactory.createToolWindowContent().
 * Gardé pour ne pas casser plugin.xml.
 */
public class LogViewerStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        // intentionally empty
    }

}
