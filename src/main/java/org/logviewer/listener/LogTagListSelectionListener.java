package org.logviewer.listener;

import org.logviewer.entity.LogTag;

import java.util.EventListener;

public interface LogTagListSelectionListener extends EventListener {

    void tagAdded(LogTag tag);

    void tagRemoved(LogTag tag);
}
