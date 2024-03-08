package io.github.bbortt.k6.report.ingress.service;

import io.github.bbortt.k6.report.ingress.domain.ReportProcessing;
import io.github.bbortt.k6.report.ingress.domain.repository.ReportProcessingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReportProcessingService {

    private final ReportProcessingRepository reportProcessingRepository;

    public ReportProcessingService(ReportProcessingRepository reportProcessingRepository) {
        this.reportProcessingRepository = reportProcessingRepository;
    }

    @Transactional
    public String generateProcessingId() {
        log.info("Starting new report parsing process");

        var processingId = reportProcessingRepository.save(new ReportProcessing())
                .getId();

        log.debug("Processing ID: {}", processingId);

        return processingId.toString();
    }

    @Transactional
    public void completeProcessing(String processingId) {
        log.info("Completing processing ID: {}", processingId);
        reportProcessingRepository.updateProcessingStatus(processingId, ReportProcessing.ProcessingStatus.SUCCESS);
    }

    @Transactional
    public void processingFailed(String processingId, String errorMessage) {
        log.info("Processing ID failed: {}", processingId);
        reportProcessingRepository.updateProcessingStatus(processingId, ReportProcessing.ProcessingStatus.FAILED, errorMessage);
    }
}
