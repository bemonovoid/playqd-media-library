package io.playqd.persistence.jpa.repository;

import io.playqd.exception.DatabaseEntityNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IdentityJpaRepository<T> extends JpaRepository<T, Long> {

  default T get(long id) {
    try {
      return getReferenceById(id);
    } catch (EntityNotFoundException e) {
      return findById(id).orElseThrow(() -> new DatabaseEntityNotFoundException(e.getMessage(), e));
    }
  }
}
