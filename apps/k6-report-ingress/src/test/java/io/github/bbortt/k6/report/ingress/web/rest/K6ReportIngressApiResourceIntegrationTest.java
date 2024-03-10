package io.github.bbortt.k6.report.ingress.web.rest;

import io.github.bbortt.k6.report.ingress.IntegrationTestWithDatasource;
import io.github.bbortt.k6.report.ingress.domain.Sample;
import io.github.bbortt.k6.report.ingress.domain.repository.ReportProcessingRepository;
import io.github.bbortt.k6.report.ingress.domain.repository.SampleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.bbortt.k6.report.ingress.domain.ReportProcessing.ProcessingStatus.SUCCESS;
import static io.github.bbortt.k6.report.ingress.web.rest.K6ReportIngressApiResource.PROCESSING_ID_HEADER_NAME;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class K6ReportIngressApiResourceIntegrationTest extends IntegrationTestWithDatasource {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportProcessingRepository reportProcessingRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Test
    void testReportUpload() throws Exception {
        var jsonReportResource = new ClassPathResource("report.json");

        try (var inputStream = jsonReportResource.getInputStream()) {
            MockMultipartFile jsonReport = new MockMultipartFile("reportFile", jsonReportResource.getFilename(), MULTIPART_FORM_DATA_VALUE, inputStream);

            var processingIdHeader = mockMvc.perform(
                            multipart("/api/rest/v1/k6/reports")
                                    .file(jsonReport)
                    )
                    .andExpect(status().isAccepted())
                    .andExpect(header().exists(PROCESSING_ID_HEADER_NAME))
                    .andReturn()
                    .getResponse()
                    .getHeader(PROCESSING_ID_HEADER_NAME);

            AtomicReference<UUID> processingId = new AtomicReference<>();

            assertThat(processingIdHeader)
                    .isNotNull()
                    .satisfies(h -> assertThatCode(() -> processingId.set(UUID.fromString(processingIdHeader)))
                            .withFailMessage("Processing-ID '%s' is not a valid UUID", processingIdHeader)
                            .doesNotThrowAnyException()
                    );

            await().atMost(5, SECONDS).until(processingCompleted(processingId.get()));

            assertThat(sampleRepository.findAllByReportProcessingIdEquals(processingId.get()))
                    .hasSize(1)
                    .first()
                    .satisfies(
                            s -> assertThat(s)
                                    .extracting(Sample::getMetric)
                                    .isEqualTo("http_reqs"),
                            s -> assertThat(s)
                                    .extracting(Sample::getValue)
                                    .isEqualTo(1.0f)
                    );
        }
    }

    private Callable<Boolean> processingCompleted(UUID reportUuid) {
        return () -> reportProcessingRepository.findById(reportUuid)
                .map(reportProcessing -> SUCCESS.equals(reportProcessing.getProcessingStatus()))
                .orElse(false);
    }
}
