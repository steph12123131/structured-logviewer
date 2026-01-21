package org.logviewer.filter;

import org.logviewer.entity.Log;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

@AllArgsConstructor
public class LogTagUpperFilter implements Predicate<Log> {

    private final List<String> path;
    private final String value;
    @Override
    public boolean test(Log log) {
         return log.getValue(path).map(o -> o.toString().equals(value)).orElse(false);

    }
}
