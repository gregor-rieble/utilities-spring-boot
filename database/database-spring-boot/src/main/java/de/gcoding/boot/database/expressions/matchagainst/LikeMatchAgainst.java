package de.gcoding.boot.database.expressions.matchagainst;

import org.hibernate.dialect.Dialect;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicType;

import java.util.List;

public class LikeMatchAgainst extends AbstractMatchAgainstDescriptor {

    LikeMatchAgainst(Dialect dialect, BasicType<Boolean> returnType) {
        super(dialect, returnType);
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, ReturnableType<?> returnType, SqlAstTranslator<?> walker) {
        final var pattern = sqlAstArguments.getFirst();

        sqlAppender.appendSql("(");
        for (var i = 1; i < sqlAstArguments.size(); i++) {
            if (i > 1) {
                sqlAppender.appendSql(" or ");
            }

            final var field = sqlAstArguments.get(i);
            renderSingleLike(sqlAppender, walker, field, pattern);
        }
        sqlAppender.appendSql(")");
    }

    private void renderSingleLike(SqlAppender sqlAppender, SqlAstTranslator<?> walker, SqlAstNode field, SqlAstNode pattern) {
        if (dialect.supportsCaseInsensitiveLike()) {
            renderCaseInsensitiveLike(sqlAppender, walker, field, pattern);
        } else {
            renderCaseSensitiveLike(sqlAppender, walker, field, pattern);
        }

    }

    private void renderCaseInsensitiveLike(SqlAppender sqlAppender, SqlAstTranslator<?> walker, SqlAstNode field, SqlAstNode pattern) {
        field.accept(walker);
        sqlAppender.appendSql(' ');
        sqlAppender.appendSql(dialect.getCaseInsensitiveLike());
        sqlAppender.appendSql(' ');
        pattern.accept(walker);
        sqlAppender.appendSql(" escape '\\'");
    }

    private void renderCaseSensitiveLike(SqlAppender sqlAppender, SqlAstTranslator<?> walker, SqlAstNode field, SqlAstNode pattern) {
        final var lower = dialect.getLowercaseFunction();

        sqlAppender.appendSql(lower);
        sqlAppender.appendSql('(');
        field.accept(walker);
        sqlAppender.appendSql(')');

        sqlAppender.appendSql(" like ");

        sqlAppender.appendSql(lower);
        sqlAppender.appendSql('(');
        pattern.accept(walker);
        sqlAppender.appendSql(") escape '\\'");
    }
}
