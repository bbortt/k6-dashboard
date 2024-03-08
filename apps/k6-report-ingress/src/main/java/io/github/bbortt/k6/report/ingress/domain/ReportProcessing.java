package io.github.bbortt.k6.report.ingress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static java.time.Instant.now;

@Data
@Entity
@Builder
@Table(name = "report_processing")
@NoArgsConstructor
@AllArgsConstructor
public class ReportProcessing {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant startTime = now();

    @Column(name = "end_time",  columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant endTime;

    @Column(name = "processing_status")
    private ProcessingStatus processingStatus = ProcessingStatus.PROCESSING;

    @Column(name = "error_message")
    private String errorMessage;

    public enum ProcessingStatus {
        PROCESSING(0),
        SUCCESS(1),
        FAILED(2);

        private int id;

        ProcessingStatus(int id) {
            this.id = id;
        }
    }
}
