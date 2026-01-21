package org.logviewer.listener;

import java.util.EventListener;
import java.util.List;

public interface LogNameListener extends EventListener {

    void logNameChanged(List<String> logName);
}
