package org.logviewer.model;

import org.logviewer.Settings;
import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.listener.LogTagListSelectionListener;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class LogTableModel extends AbstractTableModel implements LogTagListSelectionListener {

    private final Settings settings;

    public LogTableModel(Settings settings) {
        this.settings = settings;
        this.columns.addAll(settings.getColumns());
    }

    public void clear() {
        this.columns.clear();
        this.columns.addAll(settings.getColumns());
        fireTableStructureChanged();
    }

    @Override
    public void tagAdded(LogTag tag) {
        columns.add(tag);
        fireTableStructureChanged();
    }

    @Override
    public void tagRemoved(LogTag tag) {
        columns.remove(tag);
        fireTableStructureChanged();
    }


    private final List<LogTag> columns = new ArrayList<>();

    public void setLogs(List<Log> logs) {
        this.logs = logs;
        fireTableDataChanged();
    }

    private List<Log> logs = new ArrayList<>();

    @Override
    public int getRowCount() {
        return logs.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getPathToString();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        LogTag column = columns.get(columnIndex);
        Object result = switch (column.getPathToString()) {
            case "level" -> logs.get(rowIndex).getLevel();
            case "time" -> logs.get(rowIndex).getTimestamp();
            case "message" -> logs.get(rowIndex).getMessage();
            case "logger_name" -> logs.get(rowIndex).getLoggerName();
            default -> null;
        };

        if (result == null) {
            Map<String, ?> current = logs.get(rowIndex).getAdditional();

            for (String p : column.getPath()) {
                result = current.get(p);
                if (result instanceof Map<?, ?> map) {
                    current = (Map<String, ?>) map;
                }

            }

        }
        return result;

    }
}
