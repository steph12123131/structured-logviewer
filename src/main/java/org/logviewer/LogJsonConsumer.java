package org.logviewer;


import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.logviewer.entity.Log;
import org.logviewer.helper.LogHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@Slf4j
public class LogJsonConsumer implements Consumer<String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LogJsonHandler handler;
    private final Project project;

    private final StringBuilder buffer = new StringBuilder();
    private long lastAppendTimestamp = 0;

    private final Object lock = new Object();
    private final ScheduledExecutorService scheduler;

    public LogJsonConsumer(Project project, LogJsonHandler handler) {
        this.handler = handler;
        this.project = project;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "log-json-watchdog");
            t.setDaemon(true);
            return t;
        });

        // Watchdog : si le buffer stagne depuis plus d'1s, on le vide
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (lock) {
                if (buffer.length() > 0 && System.currentTimeMillis() - lastAppendTimestamp > 1000) {
                    log.warn("LogJsonConsumer watchdog: discarding stale buffer ({} chars)", buffer.length());
                    buffer.setLength(0);
                }
            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void accept(String s) {
        synchronized (lock) {
            buffer.append(s);
            lastAppendTimestamp = System.currentTimeMillis();
            tryParse();
        }
    }

    /**
     * Tente d'extraire un ou plusieurs objets JSON complets du buffer.
     * Utilise Jackson en mode streaming : si readTree réussit, il nous indique
     * exactement combien de caractères ont été consommés via getCharOffset().
     * Si le JSON est incomplet, JsonEOFException est levée et on attend plus de données.
     * Tout contenu non-JSON en préfixe est sauté jusqu'au prochain '{'.
     */
    private void tryParse() {
        while (true) {
            // Trouver le début du prochain objet JSON
            int start = buffer.indexOf("{");
            if (start < 0) {
                buffer.setLength(0); // rien de parsable, vider
                return;
            }
            if (start > 0) {
                buffer.delete(0, start); // sauter le non-JSON en préfixe
            }

            String attempt = buffer.toString();
            try {
                JsonParser parser = MAPPER.getFactory().createParser(attempt);
                JsonNode node = MAPPER.readTree(parser);

                // Jackson a consommé un objet complet
                int consumed = (int) parser.getCurrentLocation().getCharOffset();
                buffer.delete(0, consumed);

                // Tenter de décoder en Log
                dispatchNode(node);

                // Il peut y avoir d'autres objets JSON dans le buffer → boucler
            } catch (JsonEOFException e) {
                // Objet JSON incomplet, attendre plus de données
                return;
            } catch (IOException e) {
                // Contenu non parsable (ex: ligne de log texte au milieu)
                // Sauter jusqu'au prochain '{' après la position courante
                int nextBrace = buffer.indexOf("{", 1);
                if (nextBrace < 0) {
                    buffer.setLength(0);
                    return;
                }
                buffer.delete(0, nextBrace);
                // Réessayer
            }
        }
    }

    private boolean invalid(Log log)
    {
        return log.getTimestamp()==null && log.getLoggerName()==null  && log.getLevel()==null;
    }

    private void dispatchNode(JsonNode node) {
        try {
            Log logEntry = LogHelper.decode(
                    new ByteArrayInputStream(node.toString().getBytes(StandardCharsets.UTF_8))
            );
            if (invalid(logEntry)) {
                return;
            }
            logEntry.setProject(project);
            handler.handle(logEntry);
        } catch (IOException e) {
            log.warn("Cannot decode node as Log: {}", node, e);
        }
    }

    public void close() {
        scheduler.shutdown();
    }
}
