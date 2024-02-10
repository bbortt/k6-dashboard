package io.github.bbortt.k6.dashboard.domain.repository;

import io.github.bbortt.k6.dashboard.domain.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
}
