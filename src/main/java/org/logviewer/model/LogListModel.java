package org.logviewer.model;

import org.logviewer.entity.Log;

import javax.swing.*;
import java.util.List;

public class LogListModel extends AbstractListModel<Log> {

    private List<Log> logs=List.of();

    public LogListModel() {
    }

    @Override
    public int getSize() {
        return logs.size();
    }

    @Override
    public Log getElementAt(int index) {
        return logs.get(index);
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
        fireContentsChanged(this, 0, logs.size());
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void refresh() {
        if (!logs.isEmpty()) {
            fireContentsChanged(this, 0, logs.size() - 1);
        }
    }
}
