package de.gcoding.boot.database.model;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractBaseEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private final UUID id = UUID.randomUUID();

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime created;

    @LastModifiedBy
    @Column(nullable = false, length = 100)
    private String modifiedBy;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime modified;

    @Version
    @Column(nullable = false)
    @SuppressWarnings("java:S1170")
    private final Long version = null;

    public UUID getId() {
        return id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public OffsetDateTime getModified() {
        return modified;
    }

    public Long getVersion() {
        // hibernate framework will set version, potential IDE warning can be ignored
        return version;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof AbstractBaseEntity otherEntity) {
            return Objects.equals(getId(), otherEntity.getId());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}
