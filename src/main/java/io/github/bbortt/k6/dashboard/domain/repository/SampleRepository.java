package io.github.bbortt.k6.dashboard.domain.repository;

import io.github.bbortt.k6.dashboard.domain.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
}
