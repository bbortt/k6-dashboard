package io.github.bbortt.k6.report.ingress.domain.repository;

import io.github.bbortt.k6.report.ingress.domain.ReportProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportProcessingRepository extends JpaRepository<ReportProcessing, UUID> {

    @Modifying
    @Query("UPDATE ReportProcessing SET processingStatus = :processingStatus WHERE id = :id")
    void updateProcessingStatus(@Param("id") String processingId, @Param("processingStatus") ReportProcessing.ProcessingStatus processingStatus);

    @Modifying
    @Query("UPDATE ReportProcessing SET processingStatus = :processingStatus, errorMessage = :errorMessage WHERE id = :id")
    void updateProcessingStatus(@Param("id") String processingId, @Param("processingStatus") ReportProcessing.ProcessingStatus processingStatus, @Param("errorMessage") String errorMessage);
}
