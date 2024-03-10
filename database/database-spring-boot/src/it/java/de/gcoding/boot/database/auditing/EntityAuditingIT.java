package de.gcoding.boot.database.auditing;

import de.gcoding.boot.common.ThrowingRunnable;
import de.gcoding.boot.database.auditing.EntityAuditingIT.DatabaseITConfiguration;
import de.gcoding.boot.database.model.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static de.gcoding.boot.database.auditing.FixedNameAuditorAware.DEFAULT_SYSTEM_AUDITOR;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DatabaseITConfiguration.class)
class EntityAuditingIT {
    static final Clock CLOCK_IN_THE_PAST = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());
    static final AdaptableClock PROVIDER_CLOCK = new AdaptableClock();

    @Autowired
    MockEntityRepository mockEntityRepository;

    @BeforeEach
    void beforeEach() {
        PROVIDER_CLOCK.systemDefault();
    }

    @Test
    void versionIsSetToZeroAfterInitialInsert() {
        final var entity = new MockEntity();

        final var savedEntity = mockEntityRepository.save(entity);

        assertThat(savedEntity.getVersion()).isNotNull().isZero();
    }

    @Test
    void idIsSetToTheInitiallyGeneratedValueAfterInitialInsert() {
        final var entity = new MockEntity();
        final var initialId = entity.getId();

        final var savedEntity = mockEntityRepository.save(entity);
        final var idAfterSave = savedEntity.getId();

        assertThat(idAfterSave).isEqualTo(initialId);
    }

    @Test
    void createdTimestampIsSetAfterInitialSave() {
        final var entity = new MockEntity();
        final var timeBefore = OffsetDateTime.now();

        final var savedEntity = mockEntityRepository.save(entity);
        final var timeAfter = OffsetDateTime.now();

        assertThat(savedEntity.getCreated()).isBetween(timeBefore, timeAfter);
    }

    @Test
    void modifiedTimestampIsSetAfterInitialSave() {
        final var entity = new MockEntity();
        final var timeBefore = OffsetDateTime.now();

        final var savedEntity = mockEntityRepository.save(entity);
        final var timeAfter = OffsetDateTime.now();

        assertThat(savedEntity.getModified()).isBetween(timeBefore, timeAfter);
    }

    @Test
    void createdByIsSetToSystemAuditorAfterInitialSave() {
        final var entity = new MockEntity();

        final var savedEntity = mockEntityRepository.save(entity);

        assertThat(savedEntity.getCreatedBy()).isEqualTo(DEFAULT_SYSTEM_AUDITOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void createdByIsSetToCurrentPrincipalName(String principal) {
        runWithPrincipal(principal, () -> {
            final var entity = new MockEntity();

            final var savedEntity = mockEntityRepository.save(entity);

            assertThat(savedEntity.getCreatedBy()).isEqualTo(principal);
        });
    }

    @Test
    void modifiedByIsSetToSystemAuditorAfterInitialSave() {
        final var entity = new MockEntity();

        final var savedEntity = mockEntityRepository.save(entity);

        assertThat(savedEntity.getModifiedBy()).isEqualTo(DEFAULT_SYSTEM_AUDITOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void modifiedByIsSetToCurrentPrincipalName(String principal) {
        runWithPrincipal(principal, () -> {
            final var entity = new MockEntity();

            final var savedEntity = mockEntityRepository.save(entity);

            assertThat(savedEntity.getModifiedBy()).isEqualTo(principal);
        });
    }

    @Test
    void isDoesNotChangeAfterUpdate() {
        final var entity = givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow();
        final var idBeforeUpdate = entity.getId();

        final var updatedEntity = mockEntityRepository.save(entity);
        final var idAfterUpdate = updatedEntity.getId();

        assertThat(idAfterUpdate).isEqualTo(idBeforeUpdate);
    }

    @Test
    void createdTimestampIsNotUpdatedAfterUpdate() {
        final var entity = givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow();
        final var createdTimestampBeforeUpdate = entity.getCreated();

        final var updatedEntity = mockEntityRepository.save(entity);
        final var createdTimestampAfterUpdate = updatedEntity.getCreated();

        assertThat(createdTimestampBeforeUpdate).isEqualTo(createdTimestampAfterUpdate);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void createdByIsNotModifiedAfterUpdate(String principal) {
        final var entity = givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow();
        final var initialAuditor = entity.getCreatedBy();

        runWithPrincipal(principal, () -> {
            final var savedEntity = mockEntityRepository.save(entity);

            assertThat(savedEntity.getCreatedBy())
                .isNotEqualTo(principal)
                .isEqualTo(initialAuditor);
        });
    }

    @Test
    void modifiedTimestampIsUpdatedAfterUpdate() {
        final var entity = givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow();
        final var modifiedTimestampBeforeUpdate = entity.getModified();

        final var updatedEntity = mockEntityRepository.save(entity);
        final var modifiedTimestampAfterUpdate = updatedEntity.getModified();

        assertThat(modifiedTimestampBeforeUpdate)
            .isNotEqualTo(modifiedTimestampAfterUpdate)
            .isBefore(modifiedTimestampAfterUpdate);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void modifiedByIsModifiedAfterUpdate(String principal) {
        final var entity = givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow();
        final var initialAuditor = entity.getModifiedBy();

        runWithPrincipal(principal, () -> {
            final var savedEntity = mockEntityRepository.save(entity);

            assertThat(savedEntity.getModifiedBy())
                .isNotEqualTo(initialAuditor)
                .isEqualTo(principal);
        });
    }

    @Test
    void versionIsIncrementedAfterUpdate() {
        final var entity = givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow();
        final var versionBeforeUpdate = entity.getVersion();

        final var updatedEntity = mockEntityRepository.save(entity);
        final var versionAfterUpdate = updatedEntity.getVersion();

        assertThat(versionAfterUpdate)
            .isGreaterThan(versionBeforeUpdate);
    }

    private MockEntity givenAnExistingEntityCreatedInThePast() {
        PROVIDER_CLOCK.fixedInPast();
        try {
            final var entity = new MockEntity();
            return mockEntityRepository.save(entity);
        } finally {
            PROVIDER_CLOCK.systemDefault();
        }
    }

    private MockEntity givenAnExistingEntityCreatedInThePastThatWasModifiedJustNow() {
        final var entity = givenAnExistingEntityCreatedInThePast();
        entity.hasBeenModifiedJustNow = true;

        return entity;
    }

    private void runWithPrincipal(String principalName, ThrowingRunnable runnable) {
        final var authBefore = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principalName, "***", Set.of()));
        try {
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(authBefore);
        }
    }

    @SpringBootApplication
    @EntityScan(basePackageClasses = MockEntity.class)
    @EnableJpaRepositories(basePackageClasses = MockEntityRepository.class, considerNestedRepositories = true)
    @EnableJpaAuditing(auditorAwareRef = "principalIdAuditorAware", dateTimeProviderRef = "offsetDateTimeProvider")
    public static class DatabaseITConfiguration {
        @Bean
        public AuditorAware<String> principalIdAuditorAware() {
            return new PrincipalNameAuditorAware();
        }

        @Bean
        public DateTimeProvider offsetDateTimeProvider() {
            return new OffsetDateTimeProvider(PROVIDER_CLOCK);
        }
    }

    @Entity
    public static class MockEntity extends AbstractBaseEntity {
        @Column
        @SuppressWarnings("unused")
        private boolean hasBeenModifiedJustNow = false;
    }

    public interface MockEntityRepository extends JpaRepository<MockEntity, UUID> {
    }

    public static class AdaptableClock extends Clock {
        private Clock clock = Clock.systemDefaultZone();

        public void fixedInPast() {
            this.clock = CLOCK_IN_THE_PAST;
        }

        public void systemDefault() {
            this.clock = Clock.systemDefaultZone();
        }

        @Override
        public ZoneId getZone() {
            return clock.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return clock.withZone(zone);
        }

        @Override
        public Instant instant() {
            return clock.instant();
        }
    }
}
