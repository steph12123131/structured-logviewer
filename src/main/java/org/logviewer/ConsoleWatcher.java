package org.logviewer;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.util.messages.MessageBusConnection;
import org.logviewer.entity.Log;
import org.logviewer.listener.LogListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsoleWatcher implements Disposable {

    private final Project project;

    // Utiliser CopyOnWriteArrayList pour la thread-safety
    private final List<LogListener> listeners = new CopyOnWriteArrayList<>();

    private final LogJsonConsumer consumer;

    private final MessageBusConnection connection;

    public ConsoleWatcher(Project project) {
        this.project = project;
        this.consumer = new LogJsonConsumer(project, this::fireLog);

        // Créer la connexion et l'enregistrer pour disposal
        this.connection = project.getMessageBus().connect();

        // IMPORTANT : Enregistrer ce watcher pour être disposé avec le projet
        Disposer.register(project, this);

        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processStarted(@NotNull String executorId,
                                       @NotNull ExecutionEnvironment env,
                                       @NotNull ProcessHandler handler) {
                // Vérifier que le projet est toujours actif
                if (project.isDisposed()) {
                    return;
                }

                // Vérifier que c'est bien un process de CE projet
                if (env.getProject() == project) {
                    attachListener(handler);
                }
            }

            @Override
            public void processTerminated(@NotNull String executorId,
                                          @NotNull ExecutionEnvironment env,
                                          @NotNull ProcessHandler handler,
                                          int exitCode) {
                if (env.getProject() == project) {
                    System.out.println("Process terminé dans " + project.getName() + ": " + exitCode);
                }
            }
        });
    }

    public void addLogListener(LogListener listener) {
        listeners.add(listener);
    }

    public void removeLogListener(LogListener listener) {
        listeners.remove(listener);
    }

    private void fireLog(Log log) {
        // Vérifier que le projet est toujours actif avant de notifier
        if (project.isDisposed()) {
            return;
        }

        listeners.forEach(l -> {
            try {
                l.logAdded(log);
            } catch (Exception e) {
                System.err.println("Erreur lors de la notification du listener: " + e.getMessage());
            }
        });
    }

    private void attachListener(ProcessHandler handler) {
        ProcessAdapter adapter = new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                // Double vérification : projet actif et handler pas encore détaché
                if (project.isDisposed() || handler.isProcessTerminated()) {
                    return;
                }

                String chunk = event.getText();
                consumer.accept(chunk);

                System.out.print("[" + project.getName() + "][" + outputType + "] " + chunk);
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                // Nettoyer le listener quand le process se termine
                handler.removeProcessListener(this);
            }
        };

        handler.addProcessListener(adapter);
    }

    @Override
    public void dispose() {
        // Nettoyer la connexion au MessageBus
        connection.disconnect();

        // Vider la liste des listeners
        listeners.clear();

        System.out.println("ConsoleWatcher disposé pour le projet: " + project.getName());
    }
}