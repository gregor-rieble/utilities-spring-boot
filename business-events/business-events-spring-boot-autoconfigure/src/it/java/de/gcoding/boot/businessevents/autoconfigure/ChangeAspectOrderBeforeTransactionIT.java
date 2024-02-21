package de.gcoding.boot.businessevents.autoconfigure;

import de.gcoding.boot.businessevents.autoconfigure.EmitBusinessEventsIT.MockTransactionManager;
import de.gcoding.boot.businessevents.test.BusinessEventRecorder;
import de.gcoding.boot.businessevents.test.BusinessEventsTest;
import de.gcoding.boot.businessevents.test.EventEmittingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive;

@SpringBootTest(classes = {BusinessEventsAutoConfiguration.class, MockTransactionManager.class, AopAutoConfiguration.class})
@TestPropertySource(
    properties = {
        "gcoding.business-events.emission.aspect.order=1"
    }
)
@BusinessEventsTest
@EnableTransactionManagement
class ChangeAspectOrderBeforeTransactionIT {
    @Autowired
    EventEmittingService eventEmittingService;
    @Autowired
    BusinessEventRecorder businessEventRecorder;

    @BeforeEach
    void beforeEach() {
        businessEventRecorder.reset();
    }

    @Test
    void whenOrderIsHigherThanTransactionalEventIsEmittedOutsideOfSpringTransaction() {
        final var noTransactionWasInitiallyActive = !isActualTransactionActive();
        final var eventWasEmittedWithinATransaction = new AtomicBoolean(false);
        businessEventRecorder.addBusinessEventListener(event -> {
            final var isTransactionActive = isActualTransactionActive();
            eventWasEmittedWithinATransaction.set(isTransactionActive);
        });

        final var expectedPayload = eventEmittingService.emitEventWithinSpringTransactional("payload");

        assertThat(noTransactionWasInitiallyActive).isTrue();
        assertThat(eventWasEmittedWithinATransaction).isFalse();
        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithPayload(expectedPayload);
    }

    @Test
    void whenOrderIsHigherThanTransactionalEventIsEmittedOutsideOfJakartaTransaction() {
        final var noTransactionWasInitiallyActive = !isActualTransactionActive();
        final var eventWasEmittedWithinATransaction = new AtomicBoolean(false);
        businessEventRecorder.addBusinessEventListener(event -> {
            final var isTransactionActive = isActualTransactionActive();
            eventWasEmittedWithinATransaction.set(isTransactionActive);
        });

        final var expectedPayload = eventEmittingService.emitEventWithinJakartaTransactional("payload");

        assertThat(noTransactionWasInitiallyActive).isTrue();
        assertThat(eventWasEmittedWithinATransaction).isFalse();
        businessEventRecorder.assertThat().exactlyOneEventWasEmittedWithPayload(expectedPayload);
    }
}
