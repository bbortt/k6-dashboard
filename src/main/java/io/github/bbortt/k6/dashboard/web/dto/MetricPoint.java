package io.github.bbortt.k6.dashboard.web.dto;

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
        private List<Object> thresholds; // Adjust the type based on actual data
        private Object submetrics; // Adjust the type based on actual data
        private Map<String, Object> tags;
        private Double value;
        private Instant time;
    }

    private enum MetricPointType {
        Metric,
        Point
    }
}
