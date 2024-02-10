package io.github.bbortt.k6.dashboard.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"io.github.bbortt.k6.dashboard.domain.repository"})
public class JpaRepositoryConfiguration {
}
