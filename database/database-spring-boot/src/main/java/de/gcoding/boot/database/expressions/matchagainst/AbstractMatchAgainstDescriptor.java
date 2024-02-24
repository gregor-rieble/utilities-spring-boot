package de.gcoding.boot.database.expressions.matchagainst;

import org.hibernate.dialect.Dialect;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionKind;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.type.BasicType;

public abstract class AbstractMatchAgainstDescriptor extends AbstractSqmSelfRenderingFunctionDescriptor {
    protected final Dialect dialect;

    protected AbstractMatchAgainstDescriptor(Dialect dialect, BasicType<Boolean> returnType) {
        super(
            MatchAgainst.FUNCTION_NAME,
            FunctionKind.NORMAL,
            StandardArgumentsValidators.min(2),
            StandardFunctionReturnTypeResolvers.invariant(returnType),
            null
        );

        this.dialect = dialect;
    }
}
