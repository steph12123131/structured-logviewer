package org.logviewer;


import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.logviewer.entity.Log;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;




@Slf4j
public class LogJsonConsumer implements Consumer<String> {


    private final LogJsonHandler handler;
    private final Project project;


    private final StringBuilder buffer = new StringBuilder();
    private int depth = 0;
    private long depthTimeStamp;


    private final Object lock = new Object();


    public LogJsonConsumer(Project project, LogJsonHandler handler) {
        this.handler = handler;
        this.project = project;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


        Runnable task = () -> {
            synchronized (lock) {
                if (depth > 0 && System.currentTimeMillis() - depthTimeStamp > 1000) {
                    depth = 0;
                    depthTimeStamp = 0;
                    log.warn("Depth Watchdog");
                }
            }
        };


        // Exécute toutes les 5 secondes (après un délai initial de 0)
        scheduler.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
    }


    @Override
    public void accept(String s) {
        synchronized (lock) {
            for (char c : s.toCharArray()) {


                if (c == '{') {
                    depth++;
                    depthTimeStamp = System.currentTimeMillis();
                }


                if (depth > 0) {
                    buffer.append(c);
                }

                if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        flush(); // ← flush immédiatement après le '}' final
                    }
                }
            }
        }
    }


    /**
     * À appeler en fin de stream pour ne pas perdre le dernier objet non terminé.
     */
    public void flush() {
        if (buffer.isEmpty()) return;
        try {
            Log logEntry = LogJsonHelper.decode(
                    new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8))
            );
            logEntry.setProject(project);
            handler.handle(logEntry);
        } catch (IOException e) {
            log.warn("Cannot decode: {}", buffer, e);
        } finally {
            buffer.setLength(0);
        }
    }
}

