package io.playqd.persistence.jpa.repository;

import io.playqd.persistence.jpa.repository.ReadOnlyRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MediaItemViewRepository<T> extends ReadOnlyRepository<T, String> {

}
