package io.playqd.persistence.jpa.repository;

import io.playqd.persistence.jpa.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PlaylistRepository extends IdentityJpaRepository<PlaylistEntity> {

  Optional<PlaylistEntity> findByUuid(String uuid);

  @Modifying
  void deleteByUuid(String uuid);
}
