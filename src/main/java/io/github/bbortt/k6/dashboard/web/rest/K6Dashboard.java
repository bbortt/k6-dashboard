package io.github.bbortt.k6.dashboard.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bbortt.k6.dashboard.domain.Sample;
import io.github.bbortt.k6.dashboard.domain.Threshold;
import io.github.bbortt.k6.dashboard.service.SampleService;
import io.github.bbortt.k6.dashboard.service.ThresholdService;
import io.github.bbortt.k6.dashboard.service.api.dto.ApiRestV1K6SamplesGet200ResponseInner;
import io.github.bbortt.k6.dashboard.web.api.K6DashboardApiDelegate;
import io.github.bbortt.k6.dashboard.web.dto.MetricPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static io.github.bbortt.k6.dashboard.web.dto.MetricPoint.MetricPointType.Metric;
import static io.github.bbortt.k6.dashboard.web.dto.MetricPoint.MetricPointType.Point;
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
    private final ThresholdService thresholdService;

    public K6Dashboard(ObjectMapper objectMapper, SampleService sampleService, ThresholdService thresholdService) {
        this.objectMapper = objectMapper;
        this.sampleService = sampleService;
        this.thresholdService = thresholdService;
    }

    private static Sample toSample(MetricPoint metricPoint) {
        return Sample.builder()
                .ts(metricPoint.getData().getTime())
                .metric(metricPoint.getMetric())
                .tags(metricPoint.getData().getTags())
                .value(metricPoint.getData().getValue().floatValue())
                .build();
    }

    private static Threshold toThreshold(MetricPoint metricPoint) {
        return Threshold.builder()
                .ts(metricPoint.getData().getTime())
                .metric(metricPoint.getMetric())
                .tags(metricPoint.getData().getTags())
                // TODO: .threshold(metricPoint.getData().getThresholds())
                //  .abortOnFail()
                //  .delayAbortEval()
                .lastFailed(metricPoint.getData().getTainted())
                .build();
    }

    public ResponseEntity<Void> apiRestV1K6ReportsPost(MultipartFile reportFile) {
        try (var inputStream = reportFile.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(inputStream));
             var executorService = newVirtualThreadPerTaskExecutor()) {

            var metricPointFutures = reader.lines()
                    .map(this::readMetricPointFromLine)
                    .map(task -> supplyAsync(task, executorService))
                    .toList();

            var metricPoints = allOf(metricPointFutures.toArray(CompletableFuture[]::new))
                    .thenApply(
                            ignored -> metricPointFutures.stream()
                                    .map(CompletableFuture::join)
                                    .toList()
                    ).get();

            sampleService.saveAll(
                    metricPoints.stream()
                            .filter(metricPoint -> Point.equals(metricPoint.getType()))
                            .map(K6Dashboard::toSample)
                            .toList()
            );

            thresholdService.saveAll(
                    metricPoints.stream()
                            .filter(metricPoint -> Metric.equals(metricPoint.getType()))
                            .map(K6Dashboard::toThreshold)
                            .toList()
            );

            return new ResponseEntity<>(CREATED);
        } catch (IOException e) {
            log.error("Failed to parse JSON report!", e);
            return new ResponseEntity<>(BAD_REQUEST);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<List<ApiRestV1K6SamplesGet200ResponseInner>> apiRestV1K6SamplesGet() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    private Supplier<MetricPoint> readMetricPointFromLine(String line) {
        return () -> {
            try {
                return objectMapper.readValue(line, MetricPoint.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
