package org.logviewer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.logviewer.listener.LogListener;

import java.util.List;

public class LogConsole {
    LogConsole(){
    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project p : openProjects) {
        System.out.println("Projet ouvert: " + p.getName() + " (" + p.getBasePath() + ")");
        new ConsoleWatcher(p);
    }
}
    private List<LogListener> listeners;

    void addLogListener(LogListener listener) {
        listeners.add(listener);
    }


}
