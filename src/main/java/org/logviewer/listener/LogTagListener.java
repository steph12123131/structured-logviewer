package org.logviewer.listener;

import org.logviewer.entity.LogTag;

import java.util.EventListener;

public interface LogTagListener extends EventListener {

    void logTagValueDoubleClicked(LogTag tag, Object value);
}
