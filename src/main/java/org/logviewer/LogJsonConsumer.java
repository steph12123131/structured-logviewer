package org.logviewer;

import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.logviewer.entity.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class LogJsonConsumer implements Consumer<String> {

    private final LogJsonHandler handler;
    private final Project project;

    public LogJsonConsumer(Project project, LogJsonHandler handler) {
        this.handler = handler;
        this.project = project;
    }


    enum State {

        INIT,
        END_LINE,
        START_JSON,
        IN_JSON,
        END_JSON,
    }

    boolean previousStart = false;

    StringBuilder buffer = new StringBuilder();
    private State state;


    @Override
    public void accept(String s) {
        for (byte c : s.getBytes()) {

            if (!(c == '\n' || c == '\r' || c == '\t' || c == ' ' || c=='{' || c=='}') && state == State.END_JSON) {
                state = State.INIT;
            }

            if (c == '{' && state == State.END_JSON) {
                state = State.START_JSON;
                try {
                    if (previousStart) {
                        Log log = LogJsonHelper.decode(new ByteArrayInputStream(buffer.toString().getBytes()));
                        log.setProject(project);
                        handler.handle(log);
                    }
                    previousStart = true;
                } catch (IOException e) {
                    log.warn("Cannot decode {}", buffer.toString(), e);
                }
                buffer.setLength(0);
            }

            if (c == '{') {
                state = State.START_JSON;
            }
            if (c == '}') {
                state = State.END_JSON;
            }



            buffer.append((char) c);
        }
    }


}
