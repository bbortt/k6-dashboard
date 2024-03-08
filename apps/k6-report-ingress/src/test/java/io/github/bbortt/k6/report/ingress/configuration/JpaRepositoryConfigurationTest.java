package io.github.bbortt.k6.report.ingress.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.array;

class JpaRepositoryConfigurationTest {

    @Test
    void enablesJpaRepositories() {
        assertThat(JpaRepositoryConfiguration.class)
                .hasAnnotations(Configuration.class, EnableJpaRepositories.class)
                .satisfies(jpaRepositoryConfiguration -> assertThat(jpaRepositoryConfiguration.getAnnotation(EnableJpaRepositories.class))
                        .extracting(EnableJpaRepositories::basePackages)
                        .asInstanceOf(array(String[].class))
                        .containsExactly("io.github.bbortt.k6.report.ingress.domain.repository"));
    }
}
