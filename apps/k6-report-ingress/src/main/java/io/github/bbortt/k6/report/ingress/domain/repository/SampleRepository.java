package io.github.bbortt.k6.report.ingress.domain.repository;

import io.github.bbortt.k6.report.ingress.domain.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {

    Set<Sample> findAllByReportProcessingIdEquals(@Param("id") UUID reportProcessingId);
}
