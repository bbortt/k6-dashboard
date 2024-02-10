package io.github.bbortt.k6.dashboard.web;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Component
public class K6Dashboard implements K6DashboardApiDelegate {

    private final ObjectMapper objectMapper;
    private final SampleService sampleService;

    public K6Dashboard(ObjectMapper objectMapper, SampleService sampleService) {
        this.objectMapper = objectMapper;
        this.sampleService = sampleService;
    }

    private static Sample toSample(MetricPoint metricPoint) {
        return null;
    }

    public ResponseEntity<Void> apiRestV1K6ReportsPost(MultipartFile reportFile) {
        try (var inputStream = reportFile.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(inputStream));
             var executorService = newVirtualThreadPerTaskExecutor()) {


            var futures = reader.lines()
                    .map(this::readSampleLine)
                    .map(task -> supplyAsync(task, executorService))
                    .toList();

            var samples = allOf(futures.toArray(CompletableFuture[]::new))
                    .thenApply(
                            ignored -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .toList()
                    ).get();

            sampleService.saveAll(samples);

            return new ResponseEntity<>(CREATED);
        } catch (IOException e) {
            log.error("Failed to parse JSON report!", e);
            return new ResponseEntity<>(BAD_REQUEST);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Supplier<Sample> readSampleLine(String line) {
        return () -> {
            try {
                return toSample(objectMapper.readValue(line, MetricPoint.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
