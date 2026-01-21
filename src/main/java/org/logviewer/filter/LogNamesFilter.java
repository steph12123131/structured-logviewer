package org.logviewer.filter;

import org.logviewer.entity.Log;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

@AllArgsConstructor
public class LogNamesFilter implements Predicate<Log> {
    private List<String> names;

    @Override
    public boolean test(Log log) {
        return names.stream().anyMatch(name -> log.getLoggerName().startsWith(name));
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
