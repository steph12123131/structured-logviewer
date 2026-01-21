package org.logviewer;

import org.logviewer.entity.LogTag;

import java.util.List;

public interface Settings {

    List<LogTag> getColumns();

    List<Integer> getColumnWidths();

    void clear();
}
