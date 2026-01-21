package org.logviewer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.logviewer.entity.LogTag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(
        name = "LogViewerSettings",
        storages = @Storage("logViewerSettings.xml")
)
@Getter
@Setter
@Slf4j
public class LogViewerSettings implements PersistentStateComponent<LogViewerSettings>, Settings {

    public List<LogTag> getColumns() {
        log.info(""+ columns);
        return columns;
    }

    @Override
    public void clear() {
        getColumns().clear();
        getColumns().addAll(List.of(LogTag.fromStringPath("time"), LogTag.fromStringPath("level"), LogTag.fromStringPath("message")));
    }

    // Vos propriétés de configuration
    private List<LogTag> columns = new ArrayList<>();

    private List<Integer> columnWidths = new ArrayList<>();

    // Singleton pour récupérer l'instance
    public static LogViewerSettings getInstance() {
        return ApplicationManager.getApplication()
                .getService(LogViewerSettings.class);
    }

    @Nullable
    @Override
    public LogViewerSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull LogViewerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}