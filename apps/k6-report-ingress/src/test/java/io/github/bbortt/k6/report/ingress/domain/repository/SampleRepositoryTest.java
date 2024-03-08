package io.github.bbortt.k6.report.ingress.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThat;

class SampleRepositoryTest {

    @Test
    void isRepository() {
        assertThat(SampleRepository.class)
                .hasAnnotations(Repository.class);
    }

    @Test
    void extendsJpaRepository() {
        assertThat(JpaRepository.class)
                .isAssignableFrom(SampleRepository.class);
    }
}
