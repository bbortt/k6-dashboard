package io.github.bbortt.k6.report.ingress.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static io.github.bbortt.k6.report.ingress.web.rest.K6ReportIngressApiController.PROCESSING_ID_HEADER_NAME;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class K6ReportIngressApiControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("integration-tests-db")
            .withUsername("sa")
            .withPassword("sa");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testReportUpload() throws Exception {
        var resource = new ClassPathResource("report.json");
        var reportJson = readString(resource.getFile().toPath());

        var processingId = mockMvc.perform(
                        post("/api/rest/v1/k6/reports")
                                .contentType(APPLICATION_JSON)
                                .content(reportJson)
                )
                .andExpect(status().isAccepted())
                .andExpect(header().exists(PROCESSING_ID_HEADER_NAME))
                .andReturn()
                .getResponse()
                .getHeader(PROCESSING_ID_HEADER_NAME);

        assertThatCode(() -> UUID.fromString(processingId))
                .withFailMessage("Processing-ID '%s' is not a valid UUID", processingId)
                .doesNotThrowAnyException();

    }
}
