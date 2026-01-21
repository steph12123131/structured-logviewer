package org.logviewer.filter;

import org.logviewer.entity.Log;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

@AllArgsConstructor
public class LogTagNotFilter implements Predicate<Log> {

    private final List<String> path;
     @Override
    public boolean test(Log log) {
         return log.getValue(path).isPresent();

    }
}
