package org.logviewer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogViewerToolWindowFactory implements ToolWindowFactory {

    private LogServer getLogServer() {
        if (logServer == null) {
            logServer = new LogServer();
        }
        return logServer;
    }

    public LogViewer getLogViewer() {
        if (logViewer == null) {
            LogViewerSettings settings = LogViewerSettings.getInstance();
            //settings.getColumns().clear();
            logViewer = new LogViewer(settings);
        }
        return logViewer;
    }

    private LogViewer logViewer;
    private LogServer logServer;
    private BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();


    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(getLogViewer(), "Log Viewer", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void redirectSystemStreams() {
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}

            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                logQueue.add(text);
            }
        }, true);

        System.setOut(printStream);
        System.setErr(printStream);
    }
}
