package org.logviewer.settings;

import org.logviewer.entity.LogTag;

import java.util.List;

public interface Settings {

    List<LogTag> getTags();

    List<Integer> getWidths();

    void clear();
}
