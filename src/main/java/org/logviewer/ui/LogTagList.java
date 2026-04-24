package org.logviewer.ui;

import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.listener.LogListener;
import org.logviewer.listener.LogTagListSelectionListener;
import org.logviewer.model.LogTagListModel;
import org.logviewer.model.LogTagListSelectionModel;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogTagList extends JList<LogTag> implements LogListener {

    private List<LogTagListSelectionListener> listeners = new ArrayList<>();
    private boolean suppressTagEvents = false;

    public void addTagListSelectionListener(LogTagListSelectionListener listener) {
        listeners.add(listener);
    }

    void removeTagListSelectionListener(LogTagListSelectionListener listener) {
        listeners.remove(listener);
    }

    public LogTagList(LogTagListModel logTagListModel, LogTagListSelectionModel selectionModel) {
        setCellRenderer(new StringListCellRenderer());
        setModel(logTagListModel);
        setSelectionModel(selectionModel);
        selectionModel.addListSelectionListener(e -> {
            if (suppressTagEvents || e.getValueIsAdjusting()) return;
            List<LogTag> tagList = logTagListModel.getTags().stream().toList();
            for (int index = e.getFirstIndex(); index <= e.getLastIndex(); index++) {
                if (index < 0 || index >= tagList.size()) continue;
                LogTag tag = tagList.get(index);
                if (getSelectionModel().isSelectedIndex(index)) {
                    fireTagAdded(tag);
                } else {
                    fireTagRemoved(tag);
                }
            }
        });
    }

    private void fireTagAdded(LogTag tag) {
        listeners.forEach(l -> l.tagAdded(tag));
    }

    private void fireTagRemoved(LogTag tag) {
        listeners.forEach(l -> l.tagRemoved(tag));
    }

    @Override
    public void logAdded(Log log) {
        List<LogTag> selectedTags = getSelectedValuesList();

        suppressTagEvents = true;
        try {
            getSelectionModel().clearSelection();
            LogTagListModel model = (LogTagListModel) getModel();
            model.addAll(getPaths(log));
            List<LogTag> list = model.getTags().stream().toList();
            selectedTags.stream().mapToInt(list::indexOf).filter(i -> i >= 0).forEach(index -> getSelectionModel().addSelectionInterval(index, index));
        } finally {
            suppressTagEvents = false;
        }
    }

    List<List<String>> getPaths(Log log) {
        List<List<String>> result = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        scan(log.getAdditional(), currentPath, result);
        return result;
    }

    private static void scan(Map<String, ?> additional, List<String> currentPath, List<List<String>> result) {
        for (Map.Entry<String, ?> e : additional.entrySet()) {
            currentPath.add(e.getKey());
            if (e.getValue() instanceof Map<?, ?> rawMap) {
                Map<String, ?> map = (Map<String, ?>) rawMap;
                scan(map, currentPath, result);
            } else {
                result.add(new ArrayList<>(currentPath));
            }
            currentPath.remove(currentPath.size() - 1);
        }
    }

    private static class StringListCellRenderer extends JCheckBox implements ListCellRenderer<LogTag> {
        @Override
        public Component getListCellRendererComponent(JList<? extends LogTag> list, LogTag value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(value.getPathToString());
            this.setSelected(isSelected);
            return this;
        }
    }


}
