package de.gcoding.boot.database.expressions.matchagainst;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchAgainstTest {
    @Mock
    @SuppressWarnings("unused")
    StandardServiceRegistryImpl serviceRegistry;
    @Mock
    TypeConfiguration typeConfiguration;
    @Mock
    BasicTypeRegistry basicTypeRegistry;
    @Mock
    SqmFunctionRegistry functionRegistry;
    @Mock
    JdbcTypeIndicators jdbcTypeIndicators;
    @InjectMocks
    MockFunctionContributions functionContributions;
    Dialect activeDialect;
    MatchAgainst matchAgainst = new MatchAgainst();

    static Stream<Arguments> provideDialectsAndExpectedFunctionDescriptorTypes() {
        return Stream.of(
            Arguments.of(H2Dialect.class, LikeMatchAgainst.class),
            Arguments.of(PostgreSQLDialect.class, LikeMatchAgainst.class),
            Arguments.of(MySQLDialect.class, LikeMatchAgainst.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDialectsAndExpectedFunctionDescriptorTypes")
    void whenContributionIsRequestedMatchAgainstFunctionIsRegisteredWithCorrectDescriptor(
        Class<? extends Dialect> dialectClass,
        Class<? extends SqmFunctionDescriptor> functionDescriptorType
    ) {
        givenAnActiveDialect(dialectClass);

        matchAgainst.contributeFunctions(functionContributions);

        verify(functionRegistry).register(eq(MatchAgainst.FUNCTION_NAME), any(functionDescriptorType));
    }

    @SuppressWarnings("unchecked")
    private void givenAnActiveDialect(Class<? extends Dialect> dialectClass) {
        activeDialect = sneakyThrows(() -> dialectClass.getConstructor().newInstance());

        when(typeConfiguration.getCurrentBaseSqlTypeIndicators()).thenReturn(jdbcTypeIndicators);
        when(jdbcTypeIndicators.getDialect()).thenReturn(activeDialect);
        when(typeConfiguration.getBasicTypeRegistry()).thenReturn(basicTypeRegistry);
        when(basicTypeRegistry.resolve(StandardBasicTypes.BOOLEAN)).thenReturn(mock(BasicType.class));
    }

    public static class MockFunctionContributions implements FunctionContributions {
        private final SqmFunctionRegistry functionRegistry;
        private final TypeConfiguration typeConfiguration;
        private final ServiceRegistry serviceRegistry;

        public MockFunctionContributions(SqmFunctionRegistry functionRegistry, TypeConfiguration typeConfiguration, ServiceRegistry serviceRegistry) {
            this.functionRegistry = functionRegistry;
            this.typeConfiguration = typeConfiguration;
            this.serviceRegistry = serviceRegistry;
        }

        @Override
        public SqmFunctionRegistry getFunctionRegistry() {
            return functionRegistry;
        }

        @Override
        public TypeConfiguration getTypeConfiguration() {
            return typeConfiguration;
        }

        @Override
        public ServiceRegistry getServiceRegistry() {
            return serviceRegistry;
        }
    }
}