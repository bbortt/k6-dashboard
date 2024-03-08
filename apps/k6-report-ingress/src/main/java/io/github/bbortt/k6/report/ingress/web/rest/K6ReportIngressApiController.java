package io.github.bbortt.k6.report.ingress.web.rest;

import io.github.bbortt.k6.report.ingress.service.K6ReportService;
import io.github.bbortt.k6.report.ingress.web.api.K6ReportIngressApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

import static org.springframework.http.ResponseEntity.accepted;

@Slf4j
@RestController
public class K6ReportIngressApiController implements K6ReportIngressApi {

    public static final String PROCESSING_ID_HEADER_NAME = "Processing-ID";

    private final K6ReportService k6ReportService;

    public K6ReportIngressApiController(K6ReportService k6ReportService) {
        this.k6ReportService = k6ReportService;
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> uploadJSONReport(MultipartFile reportFile) {
        return k6ReportService.processFileAsync(reportFile)
                .thenApply(processingId -> accepted().header(PROCESSING_ID_HEADER_NAME, processingId).build());
    }
}
