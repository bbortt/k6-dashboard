package io.github.bbortt.k6.report.ingress.web.rest;

import io.github.bbortt.k6.report.ingress.service.K6ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

import static io.github.bbortt.k6.report.ingress.web.rest.K6ReportIngressApiController.PROCESSING_ID_HEADER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpStatus.ACCEPTED;

@ExtendWith({MockitoExtension.class})
class K6ReportIngressApiControllerTest {

    @Mock
    private K6ReportService k6ReportServiceMock;

    @Mock
    private MultipartFile reportFileMock;

    private K6ReportIngressApiController k6ReportIngressApiController;

    @BeforeEach
    void setUp() {
        k6ReportIngressApiController = new K6ReportIngressApiController(k6ReportServiceMock);
    }

    @Test
    void uploadJSONReportRespondsDespiteBeingProcessedAsynchronously() {
        var mockProcessingId = "mockId";

        var processingFuture = new CompletableFuture<>();
        doReturn(processingFuture).when(k6ReportServiceMock).processFileAsync(reportFileMock);

        CompletableFuture<ResponseEntity<Void>> responseFuture = k6ReportIngressApiController.uploadJSONReport(reportFileMock);

        assertFalse(responseFuture.isDone());
        processingFuture.complete(mockProcessingId);

        ResponseEntity<Void> responseEntity = responseFuture.join();

        assertThat(responseEntity)
                .satisfies(
                        r -> assertThat(r)
                                .extracting(ResponseEntity::getStatusCode)
                                .isEqualTo(ACCEPTED),
                        r -> assertThat(r)
                                .extracting(ResponseEntity::getHeaders)
                                .extracting(header -> header.getFirst(PROCESSING_ID_HEADER_NAME))
                                .isEqualTo(mockProcessingId)
                );
    }
}
