package de.gcoding.boot.database.expressions;

import de.gcoding.boot.database.expressions.matchagainst.MatchAgainst;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

/**
 * Provides custom expressions that can be used when building query {@link Specification}s
 */
public final class CustomExpressions {
    private CustomExpressions() {
        // should not be instantiated
    }

    /**
     * Creates an expression that will return true, if one of the given {@code fields} match the given {@code pattern}.
     * Depending on the database that is used, the expression might be optimized with dialect specific SQL (e.g.
     * MATCh AGAINST in case of a MySQL database). If no optimization can be made, a LIKE statement will be used, instead.
     *
     * @param criteriaBuilder The criteria builder
     * @param pattern         The pattern to match against
     * @param fields          The fields that should be matched
     * @return true, if one of the given fields match the given pattern, false otherwise
     */
    public static Expression<Boolean> matchAgainst(CriteriaBuilder criteriaBuilder, String pattern, Path<?>... fields) {
        final var patternExpression = criteriaBuilder.literal(pattern);

        final var arguments = new Expression<?>[fields.length + 1];
        arguments[0] = patternExpression;
        System.arraycopy(fields, 0, arguments, 1, fields.length);

        return criteriaBuilder.function(MatchAgainst.FUNCTION_NAME, Boolean.class, arguments);
    }
}
