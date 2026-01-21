package org.logviewer;

import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.listener.LogListener;
import org.logviewer.listener.LogTagListSelectionListener;
import org.logviewer.model.LogTagListModel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogViewerTagList extends JList<LogTag> implements LogListener {

    private List<LogTagListSelectionListener> listeners = new ArrayList<>();

    void addTagListSelectionListener(LogTagListSelectionListener listener) {
        listeners.add(listener);
    }

    void removeTagListSelectionListener(LogTagListSelectionListener listener) {
        listeners.remove(listener);
    }


    private ListSelectionModel selectionModel;

    public LogViewerTagList(LogTagListModel model) {
        setCellRenderer(new StringListCellRenderer());
        setModel(model);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                int index = LogViewerTagList.this.locationToIndex(e.getPoint());
                final LogTag tag = LogViewerTagList.this.getModel().getElementAt(index);
                listeners.forEach(l -> {
                            if (tag.isActivate()) {
                                l.tagRemoved(tag);
                            } else {
                                l.tagAdded(tag);
                            }
                        }
                );

                model.toggle(index);
            }
        });
    }

    @Override
    public void logAdded(Log log) {
        ((LogTagListModel)getModel()).addAll(getPaths(log));
    }

    List<List<String>> getPaths(Log log)
    {
        List<List<String>> result= new ArrayList<>();
        List<String> currentPath= new ArrayList<>();
        scan(log.getAdditional(), currentPath, result);
        return result;
    }

    private static void scan(Map<String,?> additional, List<String> currentPath, List<List<String>> result) {
        for (Map.Entry<String,?> e: additional.entrySet())
        {
            currentPath.add(e.getKey());
            if (e.getValue() instanceof Map<?,?> rawMap)
            {
                Map<String, ?> map = (Map<String, ?>) rawMap;
                scan(map,currentPath,result);
            }
            else {
                result.add(new ArrayList<>(currentPath));
            }
            currentPath.remove(currentPath.size()-1);
        }
    }

    private static class StringListCellRenderer extends JCheckBox implements ListCellRenderer<LogTag> {
        @Override
        public Component getListCellRendererComponent(JList<? extends LogTag> list, LogTag value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(value.getPathToString());
            this.setSelected(value.isActivate());
            return this;
        }
    }


}
