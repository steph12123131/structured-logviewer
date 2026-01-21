package org.logviewer.entity;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogTag implements Comparable<LogTag> {

    @Builder.Default
    private boolean activate = false;

    @Builder.Default
    private List<String> path = new ArrayList<>();

    @Override
    public int compareTo(@NotNull LogTag o) {
        return this.getPathToString().compareTo(o.getPathToString());
    }

    public String getPathToString() {
        return String.join(".", path);
    }

    public static LogTag fromStringPath(String path) {
        return LogTag.builder()
                .path(new ArrayList<>(List.of(path.split("\\."))))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LogTag logTag = (LogTag) o;
        return Objects.equals(path, logTag.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public String toString() {
        return "LogTag{" +
                "activate=" + activate +
                ", path=" + path +
                '}';
    }
}