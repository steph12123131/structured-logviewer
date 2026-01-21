package org.logviewer.filter;

import org.logviewer.entity.Log;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@AllArgsConstructor
public class LogNameFilter implements Predicate<Log> {
    private String name;

    @Override
    public boolean test(Log log) {
        return log.getLoggerName().startsWith(name);
    }

    @Override
    public @NotNull Predicate<Log> and(@NotNull Predicate<? super Log> other) {
        return Predicate.super.and(other);
    }

    @Override
    public @NotNull Predicate<Log> negate() {
        return Predicate.super.negate();
    }

    @Override
    public @NotNull Predicate<Log> or(@NotNull Predicate<? super Log> other) {
        return Predicate.super.or(other);
    }
}
