package io.github.bbortt.k6.dashboard.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import static java.nio.file.Files.readString;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class K6DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testReportUpload() throws Exception {
        var resource = new ClassPathResource("report.json");
        var reportJson = readString(resource.getFile().toPath());

        mockMvc.perform(
                        post("/api/rest/v1/k6/reports")
                                .contentType(APPLICATION_JSON)
                                .content(reportJson)
                )
                .andExpect(status().isCreated());
    }
}
