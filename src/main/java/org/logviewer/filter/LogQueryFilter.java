package org.logviewer.filter;

import org.logviewer.entity.Log;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;


public class LogQueryFilter implements Predicate<Log> {
    private final String name;
    private @NotNull Predicate<Log> queryPredicate;

    public LogQueryFilter(String name) {
      this.name=name;
    }



    @Override
    public boolean test(Log log) {
        return log.getMessage().contains(name);
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
