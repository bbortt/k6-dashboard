package io.github.bbortt.k6.dashboard.service;

import io.github.bbortt.k6.dashboard.domain.Threshold;
import io.github.bbortt.k6.dashboard.domain.repository.ThresholdRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;

    public ThresholdService(ThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    public void saveAll(List<Threshold> thresholds) {
    }
}
