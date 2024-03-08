package io.github.bbortt.k6.report.ingress.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MetricPoint {

    private MetricPointType type;
    private Data data;
    private String metric;

    @Getter
    @Setter
    public static class Data {

        private String name;
        private String type;
        private String contains;
        private Boolean tainted;
        private List<String> thresholds;
        private Object submetrics; // TODO: Adjust the type based on actual data
        private Map<String, String> tags;
        private Double value;
        private Instant time;
    }

    public enum MetricPointType {
        Metric,
        Point
    }
}
