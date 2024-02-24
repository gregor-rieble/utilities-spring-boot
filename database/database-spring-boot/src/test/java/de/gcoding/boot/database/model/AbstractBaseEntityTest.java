package de.gcoding.boot.database.model;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static de.gcoding.boot.common.ExceptionUtils.sneakyThrows;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractBaseEntityTest {
    static final UUID TEST_ID1 = UUID.fromString("2ef559ed-f0cd-4713-add8-fa4399375fc7");
    static final UUID TEST_ID2 = UUID.fromString("d0d028d8-b0b3-481e-a767-9309d094003a");
    static final MockEntity INSTANCE1 = new MockEntity(TEST_ID1);
    static final MockEntity INSTANCE2 = new MockEntity(TEST_ID2);
    static final MockEntity INSTANCE_WITH_ID_FROM_INSTANCE1 = new MockEntity(TEST_ID1);
    final Set<UUID> generatedIds = new HashSet<>();

    @Test
    void whenComparedWithSameInstanceThenTheyAreEqual() {
        assertThat(INSTANCE1).isEqualTo(INSTANCE1).hasSameHashCodeAs(INSTANCE1);
    }

    @Test
    void whenDifferentInstancesHaveSameIdsThenTheyAreEqual() {
        assertThat(INSTANCE1).isEqualTo(INSTANCE_WITH_ID_FROM_INSTANCE1).hasSameHashCodeAs(INSTANCE_WITH_ID_FROM_INSTANCE1);
    }

    @Test
    void whenDifferentInstancesHaveDifferentIdsThenTheyAreNotEqual() {
        assertThat(INSTANCE1).isNotEqualTo(INSTANCE2);
    }

    @RepeatedTest(10)
    void whenEntityIsCreatedARandomUUIDIsAssigned() {
        final var entity = new MockEntity();
        final var id = entity.getId();

        assertThat(id).isNotNull();
        assertThat(generatedIds.add(id)).isTrue();
    }

    @Test
    void equalsReturnsFalseWithDifferentObjectTypes() {
        assertThat(INSTANCE1)
            .isNotEqualTo(new Object())
            .isNotEqualTo("string");
    }

    static class MockEntity extends AbstractBaseEntity {
        public MockEntity() {
        }

        public MockEntity(UUID id) {
            sneakyThrows(() -> {
                final var idField = AbstractBaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(this, id);
            });
        }
    }
}