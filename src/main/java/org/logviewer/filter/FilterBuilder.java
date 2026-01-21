package org.logviewer.filter;

import org.logviewer.entity.Log;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class FilterBuilder {

    enum Connector {
        AND("and"), OR("or");

        final String value;

        Connector(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Optional<Connector> fromString(String value) {
            return Arrays.stream(Connector.values()).filter(value::equals).findFirst();
        }
    }

    enum Operator {
        EQUALS("="),
        UPPER(">"),
        LOWER("<"),

        NOT("~"),
        CONTAINS("~=");


        Operator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Optional<Operator> fromString(String value) {
            return Arrays.stream(Operator.values()).filter(value::equals).findFirst();
        }

        final String value;
    }

    @Getter
    @Setter
    @Builder
    private static class Token {
        private Operator operator;
        private Connector connector;
        private String raw;

        public Predicate<Log> predicate() {
            AtomicReference<Predicate<Log>> predicate = new AtomicReference<>(log -> true);
            int indexQuote = raw.indexOf('"');
            Arrays.stream(Operator.values()).forEach(operator -> {
                int index = raw.indexOf(operator.getValue());
                if (index != -1 && (indexQuote == -1 || index < indexQuote)) {
                    String left = raw.substring(0, index);
                    String right = raw.substring(index);
                    predicate.set(switch (operator) {
                        case EQUALS -> new LogTagEqualsFilter(parsePath(left), unQuote(right.substring(1)));
                       case CONTAINS -> new LogTagContainsFilter(parsePath(left), unQuote(right.substring(2)));
                        case UPPER -> new LogTagUpperFilter(parsePath(left), unQuote(right.substring(1)));
                        case LOWER -> new LogTagLowerFilter(parsePath(left), unQuote(right.substring(1)));
                        case NOT -> new LogTagNotFilter(parsePath(right + 1));

                        default -> (Predicate<Log>) log -> true;

                    });
                }
            });


            return predicate.get();
        }

        public Optional<Connector> isConnector() {
            return Connector.fromString(raw);
        }

        private String unQuote(String substring) {
            return substring.replace("\"", "");
        }

        private List<String> parsePath(String path) {
            return Arrays.stream(path.split("\\.")).toList();
        }

        ;
    }


    List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        char quote = 0;
        int lastIndex = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c) && quote == 0) {
                String token = input.substring(lastIndex, i);

                decodeToken(token, tokens);
                lastIndex = i + 1;

            }
            if (c == '"' && quote == 0) {
                quote = c;
            }
            if (c == '"' && quote == '"') {
                quote = 0;
            }
        }
        decodeToken(input.substring(lastIndex, input.length()), tokens);
        return tokens;
    }

    private void decodeToken(String token, List<Token> tokens) {
        Optional<Connector> connector = Connector.fromString(token);
        Optional<Operator> operator = Operator.fromString(token);
        if (connector.isPresent()) {
            tokens.add(Token.builder().connector(connector.get()).build());
        } else if (operator.isPresent()) {
            tokens.add(Token.builder().operator(operator.get()).build());
        } else {
            tokens.add(Token.builder().raw(token).build());
        }
    }

    private Optional<Predicate<Log>> operation(Token token) {
        return Optional.empty();
    }

    public FilterBuilder query(String query) {
        if (query.isEmpty()) {
            predicate = log -> true;
        }
        List<Token> tokens = tokenize(query);
        AtomicReference<Connector> lastOperator = new AtomicReference<>(null);
        tokens.forEach(token -> {
            Optional<Connector> connector = token.isConnector();


            if (connector.isEmpty()) {
                Predicate<Log> tagFilter = token.predicate();
                if (lastOperator.get() == null) {
                    predicate = tagFilter;
                } else {
                    switch (lastOperator.get()) {
                        case OR:
                            predicate = predicate.or(tagFilter);
                        case AND:
                            predicate = predicate.and(tagFilter);
                    }
                }
            } else {
                lastOperator.set(connector.get());

            }

        });
        return this;
    }

    Predicate<Log> predicate = log -> true;

    public Predicate<Log> build() {
        return predicate;
    }
}
