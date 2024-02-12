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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;

@Data
@Entity
@Builder
@Table(name = "samples")
@NoArgsConstructor
@AllArgsConstructor
public class Sample {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "samples_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "samples_id_seq")
    private Long id;

    @Column(name = "ts", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant ts;

    @Column(name = "metric", nullable = false, length = 128)
    private String metric;

    @Type(JsonType.class)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, String> tags;

    @Column(name = "value")
    private Float value;
}
