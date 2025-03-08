package de.gcoding.boot.database.expressions.matchagainst;

import org.hibernate.QueryException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SpannerDialect;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.sql.ast.SqlAstWalker;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicType;
import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LikeMatchAgainstTest {
    @Mock
    BasicType<Boolean> returnType;
    @Mock
    TypeConfiguration typeConfiguration;
    Dialect dialect;
    MockSqlAppender mockSqlAppender;
    LikeMatchAgainst likeMatchAgainst;

    @BeforeEach
    void beforeEach() {
        mockSqlAppender = new MockSqlAppender();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void whenLessThanTwoArgumentsAreGivenArgumentsValidatorThrowsException(int numArguments) {
        givenADialectThatDoesNotSupportCaseInsensitiveLike();

        final var argumentTypes = createArgumentTypes(numArguments);
        final var validator = likeMatchAgainst.getArgumentsValidator();

        final var exception = assertThrows(
            QueryException.class,
            () -> validator.validate(argumentTypes, MatchAgainst.FUNCTION_NAME, typeConfiguration)
        );

        assertThat(exception).hasMessageContaining("requires at least 2 arguments");
    }

    @Test
    void whenRenderedQueryWillBeTranslatedToLikeWithLowerCasedValues() {
        givenADialectThatDoesNotSupportCaseInsensitiveLike();

        likeMatchAgainst.render(mockSqlAppender, createArguments("%pattern%", "field"), (ReturnableType<?>) null, null);

        assertThat(mockSqlAppender).hasToString("(lower(field) like lower(%pattern%))");
    }

    @Test
    void whenRenderedWithTwoFieldsQueryExpressionsWillBeConcatenatedWithOrs() {
        givenADialectThatDoesNotSupportCaseInsensitiveLike();

        likeMatchAgainst.render(
            mockSqlAppender,
            createArguments("%pattern%", "field1", "field2"),
            (ReturnableType<?>) null,
            null
        );

        assertThat(mockSqlAppender)
            .hasToString("(lower(field1) like lower(%pattern%) or lower(field2) like lower(%pattern%))");
    }

    @Test
    void whenRenderedWithThreeFieldsQueryExpressionsWillBeConcatenatedWithOrs() {
        givenADialectThatDoesNotSupportCaseInsensitiveLike();

        likeMatchAgainst.render(
            mockSqlAppender,
            createArguments("%pattern%", "field1", "field2", "field3"),
            (ReturnableType<?>) null,
            null
        );

        assertThat(mockSqlAppender)
            .hasToString("(lower(field1) like lower(%pattern%) or lower(field2) like lower(%pattern%) or lower(field3) like lower(%pattern%))");
    }

    @Test
    void whenRenderedWithDialectThatSupportsCaseInsensitiveLikeItIsUsedWithoutLowerCaseFunctionInTranslation() {
        final var caseInsensitiveLike = givenADialectThatSupportsCaseInsensitiveLike();

        likeMatchAgainst.render(mockSqlAppender, createArguments("%pattern%", "field"), (ReturnableType<?>) null, null);

        assertThat(mockSqlAppender).hasToString("(field " + caseInsensitiveLike + " %pattern%)");
    }

    private LinkedList<SqmTypedNode<?>> createArgumentTypes(int numArguments) {
        final var argumentTypes = new LinkedList<SqmTypedNode<?>>();
        for (var i = 0; i < numArguments; i++) {
            argumentTypes.add(mock(SqmTypedNode.class));
        }
        return argumentTypes;
    }

    private List<? extends SqlAstNode> createArguments(String... arguments) {
        return Arrays.stream(arguments)
            .map(arg -> new MockSqlAstNode(mockSqlAppender, arg))
            .toList();
    }

    private String givenADialectThatSupportsCaseInsensitiveLike() {
        givenTheActiveDialect(PostgreSQLDialect.class);
        final var caseInsensitiveLike = dialect.getCaseInsensitiveLike();
        assertThat(caseInsensitiveLike).isNotEqualToIgnoringCase("like");

        return caseInsensitiveLike;
    }

    private void givenADialectThatDoesNotSupportCaseInsensitiveLike() {
        givenTheActiveDialect(SpannerDialect.class);
    }

    private void givenTheActiveDialect(Class<? extends Dialect> dialectType) {
        sneakyThrows(() -> {
            dialect = dialectType.getConstructor().newInstance();
            likeMatchAgainst = new LikeMatchAgainst(dialect, returnType);
        });
    }

    static class MockSqlAppender implements SqlAppender {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void appendSql(String fragment) {
            buffer.append(fragment);
        }

        @Override
        public String toString() {
            return buffer.toString();
        }
    }

    static class MockSqlAstNode implements SqlAstNode {
        private final SqlAppender sqlAppender;
        private final String literal;

        MockSqlAstNode(SqlAppender sqlAppender, String literal) {
            this.sqlAppender = sqlAppender;
            this.literal = literal;
        }

        @Override
        public void accept(SqlAstWalker sqlTreeWalker) {
            sqlAppender.appendSql(literal);
        }
    }
}