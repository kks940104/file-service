package org.anonymous.global.entities;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-DD HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    @JsonFormat(pattern = "yyyy-MM-DD HH:mm:ss")
    private LocalDateTime modifiedAt;

    @JsonFormat(pattern = "yyyy-MM-DD HH:mm:ss")
    private LocalDateTime deletedAt;
}













