package io.github.bbortt.k6.dashboard.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;

@Data
@Entity
@Table(name = "thresholds")
@NoArgsConstructor
@AllArgsConstructor
public class Threshold {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "thresholds_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "thresholds_id_seq")
    private Long id;

    @Column(name = "ts", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant ts;

    @Column(name = "metric", nullable = false, length = 128)
    private String metric;

    @Type(JsonType.class)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, Object> tags; // Using a Map to represent the JSON structure

    @Column(name = "threshold", nullable = false, length = 128)
    private String threshold;

    @Column(name = "abort_on_fail", nullable = false)
    private Boolean abortOnFail;

    @Column(name = "delay_abort_eval", length = 128)
    private String delayAbortEval;

    @Column(name = "last_failed", nullable = false)
    private Boolean lastFailed;
}
