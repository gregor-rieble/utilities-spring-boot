package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.autoconfigure.EmitBusinessEventsIT.MockTransactionManager;
import de.gcoding.boot.businessevents.test.BusinessEventRecorder;
import de.gcoding.boot.businessevents.test.BusinessEventsTest;
import de.gcoding.boot.businessevents.test.EventEmittingService;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive;
import static org.springframework.transaction.support.TransactionSynchronizationManager.setActualTransactionActive;

@SpringBootTest(classes = {BusinessEventsAutoConfiguration.class, MockTransactionManager.class, AopAutoConfiguration.class})
@EnableTransactionManagement
@BusinessEventsTest
class EmitBusinessEventsIT {
    @Autowired
    EventEmittingService eventEmittingService;
    @Autowired
    ConfigurableApplicationContext eventPublisher;
    @Autowired
    BusinessEventRecorder businessEventRecorder;

    @BeforeEach
    void beforeEach() {
        businessEventRecorder.reset();
    }

    @Test
    void whenValueIsReturnedItIsUsedAsEventPayload() {
        final var expectedPayload = eventEmittingService.emitSimpleEvent("test");

        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithPayload(expectedPayload);
    }

    @Test
    void whenOptionalIsReturnedValueIsUnwrapped() {
        final var expectedPayload = eventEmittingService.emitOptionalEvent("test")
            .orElseThrow(IllegalStateException::new);

        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithPayload(expectedPayload);
    }

    @Test
    void whenListIsReturnedEventsAreEmittedForEachListEntry() {
        final var expectedPayloads = eventEmittingService.emitEventsForEachListItem(List.of("first", "second", "third"));

        businessEventRecorder.assertThat().eventsWhereEmittedWithPayloads(expectedPayloads);
    }

    @Test
    void whenSetIsReturnedEventsAreEmittedForEachListEntry() {
        final var expectedPayloads = eventEmittingService.emitEventsForEachSetItem(Set.of("first", "second", "third"));

        businessEventRecorder.assertThat().eventsWhereEmittedWithPayloads(expectedPayloads);
    }

    @ParameterizedTest
    @ValueSource(classes = {HashSet.class, LinkedList.class, ArrayList.class, ArrayDeque.class, LinkedBlockingQueue.class})
    void whenCollectionIsReturnedEventsAreEmittedForEachEntry(Class<? extends Collection<String>> type) throws Exception {
        Collection<String> eventPayloads = type.getConstructor().newInstance();
        eventPayloads.add("first");
        eventPayloads.add("second");
        eventPayloads.add("third");

        final var expectedPayloads = eventEmittingService.emitEventsForEachCollectionItem(eventPayloads);

        businessEventRecorder.assertThat().eventsWhereEmittedWithPayloads(expectedPayloads);
    }

    @Test
    void whenUsedWithSpringTransactionEventIsEmittedWithinTransaction() {
        final var noTransactionWasInitiallyActive = !isActualTransactionActive();
        final var eventWasEmittedWithinATransaction = new AtomicBoolean(false);
        businessEventRecorder.addBusinessEventListener(event -> {
            final var isTransactionActive = isActualTransactionActive();
            eventWasEmittedWithinATransaction.set(isTransactionActive);
        });

        final var expectedPayload = eventEmittingService.emitEventWithinSpringTransactional("payload");

        assertThat(noTransactionWasInitiallyActive).isTrue();
        assertThat(eventWasEmittedWithinATransaction).isTrue();
        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithPayload(expectedPayload);
    }

    @Test
    void whenUsedWithJakartaTransactionEventIsEmittedWithinTransaction() {
        final var noTransactionWasInitiallyActive = !isActualTransactionActive();
        final var eventWasEmittedWithinATransaction = new AtomicBoolean(false);
        businessEventRecorder.addBusinessEventListener(event -> {
            final var isTransactionActive = isActualTransactionActive();
            eventWasEmittedWithinATransaction.set(isTransactionActive);
        });

        final var expectedPayload = eventEmittingService.emitEventWithinJakartaTransactional("payload");

        assertThat(noTransactionWasInitiallyActive).isTrue();
        assertThat(eventWasEmittedWithinATransaction).isTrue();
        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithPayload(expectedPayload);
    }

    @Test
    void whenEventIsEmittedWithSpecifiedActionGeneratedEventWillHaveTheSameAction() {
        final var expectedAction = eventEmittingService.emitEventWithAction();

        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithAction(expectedAction);
    }

    @Test
    void whenEventIsEmittedWithSpELActionGeneratedEventWillHaveTheEvaluatedAction() {
        final var expectedAction = eventEmittingService.emitEventWithSpELAction();

        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithAction(expectedAction);
    }

    @Test
    void whenEventIsEmittedWithSpELActionBeansFromApplicationContextAreAccessible() {
        final var expectedAction = eventEmittingService.emitEventWithSpELActionAccessingBeans();

        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithAction(expectedAction);
    }

    @Component
    public static class MockTransactionManager implements PlatformTransactionManager {
        @Nonnull
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            setActualTransactionActive(true);
            return new SimpleTransactionStatus(true);
        }

        @Override
        public void commit(@Nonnull TransactionStatus status) throws TransactionException {
            setActualTransactionActive(false);
        }

        @Override
        public void rollback(@Nonnull TransactionStatus status) throws TransactionException {
            setActualTransactionActive(false);
        }
    }
}
