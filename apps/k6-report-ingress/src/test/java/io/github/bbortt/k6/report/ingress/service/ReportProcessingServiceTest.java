package io.github.bbortt.k6.report.ingress.service;

import io.github.bbortt.k6.report.ingress.domain.ReportProcessing;
import io.github.bbortt.k6.report.ingress.domain.repository.ReportProcessingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class ReportProcessingServiceTest {

    private static final UUID PROCESSING_UUID = UUID.fromString("7f7d7431-6e58-4dcb-b07f-90b535484867");

    @Mock
    private ReportProcessingRepository reportProcessingRepositoryMock;

    private ReportProcessingService reportProcessingService;

    @BeforeEach
    void setUp() {
        reportProcessingService = new ReportProcessingService(reportProcessingRepositoryMock);
    }

    @Test
    void isService() {
        assertThat(ReportProcessingService.class)
                .hasAnnotations(Service.class);
    }

    @Test
    void generateProcessingIdShouldReturnId() {
        var reportProcessing = ReportProcessing.builder()
                .id(PROCESSING_UUID)
                .build();

        doReturn(reportProcessing).when(reportProcessingRepositoryMock).save(any(ReportProcessing.class));

        var result = reportProcessingService.generateProcessingId();

        assertThat(result)
                .isEqualTo(PROCESSING_UUID);
    }

    @Test
    void completeProcessingShouldCallRepository() {
        reportProcessingService.completeProcessing(PROCESSING_UUID);

        verify(reportProcessingRepositoryMock).processingSucceeded(PROCESSING_UUID);
    }

    @Test
    void processingFailedShouldCallRepositoryWithErrorMessage() {
        String errorMessage = "Test Error";

        reportProcessingService.processingFailed(PROCESSING_UUID, errorMessage);

        verify(reportProcessingRepositoryMock).processingFailed(PROCESSING_UUID, errorMessage);
    }
}
