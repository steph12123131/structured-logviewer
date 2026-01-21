package org.logviewer.listener;

import java.util.EventListener;
import java.util.List;

public interface LogTableListener extends EventListener {

    void logTagValueDoubleClicked(List<String> tag, Object value);
}
