package io.github.bbortt.k6.report.ingress.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bbortt.k6.report.ingress.domain.Sample;
import io.github.bbortt.k6.report.ingress.domain.Threshold;
import io.github.bbortt.k6.report.ingress.web.dto.MetricPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static io.github.bbortt.k6.report.ingress.web.dto.MetricPoint.MetricPointType.Metric;
import static io.github.bbortt.k6.report.ingress.web.dto.MetricPoint.MetricPointType.Point;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static org.springframework.core.NestedExceptionUtils.getRootCause;

@Slf4j
@Service
public class K6ReportService {

    private final ObjectMapper objectMapper;
    private final ReportProcessingService reportProcessingService;
    private final SampleService sampleService;
    private final ThresholdService thresholdService;

    public K6ReportService(ObjectMapper objectMapper, ReportProcessingService reportProcessingService, SampleService sampleService, ThresholdService thresholdService) {
        this.objectMapper = objectMapper;
        this.reportProcessingService = reportProcessingService;
        this.sampleService = sampleService;
        this.thresholdService = thresholdService;
    }

    private static Sample toSample(io.github.bbortt.k6.report.ingress.web.dto.MetricPoint metricPoint) {
        return Sample.builder()
                .ts(metricPoint.getData().getTime())
                .metric(metricPoint.getMetric())
                .tags(metricPoint.getData().getTags())
                .value(metricPoint.getData().getValue().floatValue())
                .build();
    }

    private static io.github.bbortt.k6.report.ingress.domain.Threshold toThreshold(io.github.bbortt.k6.report.ingress.web.dto.MetricPoint metricPoint) {
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

    public CompletableFuture<String> processFileAsync(MultipartFile multipartFile) {
        log.info("Processing Uploaded JSON report: {}", multipartFile.getName());

        var processingId = reportProcessingService.generateProcessingId();

        runAsync(() -> parseAndPersistReportFile(multipartFile))
                .exceptionally(e -> {
                    log.error("Asynchronous processing failed", e);
                    reportProcessingService.processingFailed(processingId, requireNonNull(getRootCause(e)).getMessage());
                    return null;
                }).thenAccept((ignored) -> reportProcessingService.completeProcessing(processingId));

        return completedFuture(processingId);
    }

    private void parseAndPersistReportFile(MultipartFile reportFile) {
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
                            .map(K6ReportService::toSample)
                            .toList()
            );

            thresholdService.saveAll(
                    metricPoints.stream()
                            .filter(metricPoint -> Metric.equals(metricPoint.getType()))
                            .map(K6ReportService::toThreshold)
                            .toList()
            );
        } catch (IOException e) {
            log.error("Failed to parse JSON report!", e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
