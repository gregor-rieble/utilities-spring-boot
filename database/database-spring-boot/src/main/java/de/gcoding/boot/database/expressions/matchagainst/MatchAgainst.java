package de.gcoding.boot.database.expressions.matchagainst;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

public class MatchAgainst implements FunctionContributor {
    public static final String FUNCTION_NAME = "match_against";

    MatchAgainst() {
    }

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        final var dialect = functionContributions.getDialect();
        final var functionRegistry = functionContributions.getFunctionRegistry();
        final var typeRegistry = functionContributions.getTypeConfiguration().getBasicTypeRegistry();

        final var booleanType = typeRegistry.resolve(StandardBasicTypes.BOOLEAN);

        final var functionDescriptor = switch (dialect) {
            default -> new LikeMatchAgainst(dialect, booleanType);
        };

        functionRegistry.register(FUNCTION_NAME, functionDescriptor);
    }

}
