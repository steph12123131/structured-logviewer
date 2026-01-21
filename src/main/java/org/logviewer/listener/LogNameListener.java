package org.logviewer.listener;

import java.util.EventListener;

public interface LogNameListener extends EventListener {

    void logNameChanged(String logName);
}
