package io.github.bbortt.k6.report.ingress.domain.repository;

import io.github.bbortt.k6.report.ingress.IntegrationTestWithDatasource;
import io.github.bbortt.k6.report.ingress.domain.ReportProcessing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.UUID;
import java.util.function.Consumer;

import static io.github.bbortt.k6.report.ingress.domain.ReportProcessing.ProcessingStatus.FAILED;
import static io.github.bbortt.k6.report.ingress.domain.ReportProcessing.ProcessingStatus.SUCCESS;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class ReportProcessingRepositoryIntegrationTest extends IntegrationTestWithDatasource {

    private static Consumer<? super ReportProcessing> assertStatusEquals(ReportProcessing.ProcessingStatus success) {
        return (ReportProcessing resultingReportProcessing) -> assertThat(resultingReportProcessing)
                .extracting(ReportProcessing::getProcessingStatus)
                .isEqualTo(success);
    }

    private static Consumer<? super ReportProcessing> assertEndTimeAfterStartTime(ReportProcessing persistedReportProcessing) {
        return (ReportProcessing resultingReportProcessing) -> assertThat(resultingReportProcessing)
                .extracting(ReportProcessing::getEndTime)
                .isNotNull()
                .matches(endTime -> endTime.isAfter(persistedReportProcessing.getStartTime()));
    }

    @Autowired
    private ReportProcessingRepository reportProcessingRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private UUID reportProcessingId;

    @BeforeEach
    void beforeTestSetup() {
        reportProcessingId = reportProcessingRepository.save(
                        new ReportProcessing()
                )
                .getId();
    }

    @Test
    void processingSucceeded() {
        var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        assertThat(reportProcessingRepository.processingSucceeded(reportProcessingId))
                .isOne();

        transactionManager.commit(transactionStatus);

        var result = reportProcessingRepository.findById(reportProcessingId);
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(
                        assertStatusEquals(SUCCESS),
                        ReportProcessingRepositoryIntegrationTest::assertEndTimeAfterStartTime
                );
    }

    @Test
    void processingFailed() {
        var errorMessage = "error message!";

        var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        assertThat(reportProcessingRepository.processingFailed(reportProcessingId, errorMessage))
                .isOne();

        transactionManager.commit(transactionStatus);

        assertThat(reportProcessingRepository.findById(reportProcessingId))
                .isPresent()
                .get()
                .satisfies(
                        assertStatusEquals(FAILED),
                        r -> assertThat(r)
                                .extracting(ReportProcessing::getErrorMessage)
                                .isEqualTo(errorMessage),
                        ReportProcessingRepositoryIntegrationTest::assertEndTimeAfterStartTime
                );
    }

    @AfterEach
    void afterEachTeardown() {
        if (nonNull(reportProcessingId)) {
            reportProcessingRepository.deleteById(reportProcessingId);
        }
    }
}
