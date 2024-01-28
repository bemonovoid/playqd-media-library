package io.playqd.persistence.jpa.repository;

import io.playqd.persistence.jpa.entity.AudioFileSourceAuditLogJpaEntity;

import java.util.Optional;

public interface AudioFileAuditLogRepository extends IdentityJpaRepository<AudioFileSourceAuditLogJpaEntity> {

  default Optional<AudioFileSourceAuditLogJpaEntity> getOne() {
    return findById(1L);
  }
}
