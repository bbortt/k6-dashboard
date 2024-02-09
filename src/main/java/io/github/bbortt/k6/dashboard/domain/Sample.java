package io.github.bbortt.k6.dashboard.domain;

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

import java.time.Instant;
import java.util.Map;

@Data
@Entity
@Table(name = "samples")
@NoArgsConstructor
@AllArgsConstructor
public class Sample {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "sequenceGenerator")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    private Long id;

    @Column(name = "ts", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant ts;

    @Column(name = "metric", nullable = false, length = 128)
    private String metric;

    // TODO: @Type(type = "jsonb")
    //  https://github.com/vladmihalcea/hypersistence-utils
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, Object> tags;

    @Column(name = "value")
    private Float value;
}
