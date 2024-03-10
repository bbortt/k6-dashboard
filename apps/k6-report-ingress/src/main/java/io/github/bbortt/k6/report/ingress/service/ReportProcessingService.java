package io.github.bbortt.k6.report.ingress.service;

import io.github.bbortt.k6.report.ingress.domain.ReportProcessing;
import io.github.bbortt.k6.report.ingress.domain.repository.ReportProcessingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ReportProcessingService {

    private final ReportProcessingRepository reportProcessingRepository;

    public ReportProcessingService(ReportProcessingRepository reportProcessingRepository) {
        this.reportProcessingRepository = reportProcessingRepository;
    }

    @Transactional
    public ReportProcessing generateProcessingId() {
        log.info("Starting new report parsing process");

        var reportProcessing = reportProcessingRepository.save(new ReportProcessing());

        log.debug("Processing ID: {}", reportProcessing.getId());

        return reportProcessing;
    }

    @Transactional
    public void completeProcessing(UUID processingId) {
        log.info("Completing processing ID: {}", processingId);
        reportProcessingRepository.processingSucceeded(processingId);
    }

    @Transactional
    public void processingFailed(UUID processingId, String errorMessage) {
        log.info("Processing ID failed: {}", processingId);
        reportProcessingRepository.processingFailed(processingId, errorMessage);
    }
}
