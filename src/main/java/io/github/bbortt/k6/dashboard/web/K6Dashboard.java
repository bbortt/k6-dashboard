package io.github.bbortt.k6.dashboard.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bbortt.k6.dashboard.domain.Sample;
import io.github.bbortt.k6.dashboard.service.SampleService;
import io.github.bbortt.k6.dashboard.web.api.K6DashboardApiDelegate;
import io.github.bbortt.k6.dashboard.web.dto.MetricPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Component
public class K6Dashboard implements K6DashboardApiDelegate {

    private final ObjectMapper objectMapper;
    private final SampleService sampleService;

    public K6Dashboard(ObjectMapper objectMapper, SampleService sampleService) {
        this.objectMapper = objectMapper;
        this.sampleService = sampleService;
    }

    public ResponseEntity<Void> apiRestV1K6ReportsPost(MultipartFile reportFile) {
        var metricPoints = new ArrayList<Sample>();

        try (InputStream inputStream = reportFile.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // TODO: Use virtual threads!
            String line;
            while ((line = reader.readLine()) != null) {
                metricPoints.add(toSample(objectMapper.readValue(line, MetricPoint.class)));
            }

            sampleService.saveAll(metricPoints);

            return new ResponseEntity<>(CREATED);
        } catch (IOException e) {
            log.error("Failed to parse JSON report!", e);
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    private Sample toSample(MetricPoint metricPoint) {
        return null;
    }
}
