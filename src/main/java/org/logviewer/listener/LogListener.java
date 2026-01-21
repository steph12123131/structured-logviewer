package org.logviewer.listener;

import org.logviewer.entity.Log;

import java.util.EventListener;

public interface LogListener extends EventListener {

    void logAdded(Log log);
}
