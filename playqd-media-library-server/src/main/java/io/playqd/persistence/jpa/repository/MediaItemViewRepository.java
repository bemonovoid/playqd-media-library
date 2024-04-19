package io.playqd.persistence.jpa.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MediaItemViewRepository<T> extends ReadOnlyRepository<T, String> {

}
