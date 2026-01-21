package org.logviewer;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class LogJsonConsumer implements Consumer<String> {

    private final LogJsonHandler handler;

    public LogJsonConsumer(LogJsonHandler handler) {
        this.handler = handler;
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
                        handler.handle(LogJsonHelper.decode(new ByteArrayInputStream(buffer.toString().getBytes())));
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
