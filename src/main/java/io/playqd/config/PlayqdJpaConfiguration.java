package io.playqd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "io.playqd.persistence.jpa.repository")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "jpaAuditorAware")
class PlayqdJpaConfiguration {

  @Bean("jpaAuditorAware")
  AuditorAware<String> jpaAuditorAware() {
    return () -> Optional.of("system");
  }

}
