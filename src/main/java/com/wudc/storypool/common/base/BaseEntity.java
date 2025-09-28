package com.wudc.storypool.common.base;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@MappedSuperclass()
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Id
    @Column(
        length = 26,
        unique = true
    )
    @Setter
    private String id;

    @CreatedDate
    @Setter
    private Instant createdAt;

    @LastModifiedDate
    @Setter
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = new ULID().nextULID();
        }
    }

    public LocalDateTime getCreatedAtByLocalDateTime() {
        if (createdAt == null) { return null;}
        return LocalDateTime.from(createdAt.atZone(ZoneId.of("Asia/Seoul")));
    }

    public LocalDateTime getUpdatedAtByLocalDateTime() {
        if (updatedAt == null) { return null;}
        return LocalDateTime.from(updatedAt.atZone(ZoneId.of("Asia/Seoul")));
    }
}
