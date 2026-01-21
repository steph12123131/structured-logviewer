package org.logviewer;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class ConsoleWatcherService {

    private final ConsoleWatcher watcher;

    public ConsoleWatcherService(Project project) {
        this.watcher = new ConsoleWatcher(project);
    }

    public ConsoleWatcher getWatcher() {
        return watcher;
    }
}