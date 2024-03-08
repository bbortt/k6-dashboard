package io.github.bbortt.k6.report.ingress.service;

import io.github.bbortt.k6.report.ingress.domain.Threshold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
class ThresholdService {

    public void saveAll(List<Threshold> thresholds) {
        log.trace("Persisting Thresholds is currently not supported!");
    }
}
