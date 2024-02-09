package io.github.bbortt.k6.dashboard.service;

import io.github.bbortt.k6.dashboard.domain.Sample;
import io.github.bbortt.k6.dashboard.domain.repository.SampleRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Modifying
    @Transactional
    public void saveAll(ArrayList<Sample> metricPoints) {
    }
}
