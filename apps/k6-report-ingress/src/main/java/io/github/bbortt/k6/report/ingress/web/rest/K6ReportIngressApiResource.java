package io.github.bbortt.k6.report.ingress.web.rest;

import io.github.bbortt.k6.report.ingress.service.K6ReportService;
import io.github.bbortt.k6.report.ingress.web.api.K6ReportIngressApiDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.ResponseEntity.accepted;

@Slf4j
@RestController
public class K6ReportIngressApiResource implements K6ReportIngressApiDelegate {

    public static final String PROCESSING_ID_HEADER_NAME = "Processing-ID";

    private final K6ReportService k6ReportService;

    public K6ReportIngressApiResource(K6ReportService k6ReportService) {
        this.k6ReportService = k6ReportService;
    }

    @Override
    public ResponseEntity<Void> uploadJSONReport(MultipartFile reportFile) {
        var processingId = k6ReportService.processFileAsync(reportFile);
        return accepted().header(PROCESSING_ID_HEADER_NAME, processingId.toString()).build();
    }
}
