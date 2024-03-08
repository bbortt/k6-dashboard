package io.github.bbortt.k6.report.ingress.service;

import io.github.bbortt.k6.report.ingress.domain.Sample;
import io.github.bbortt.k6.report.ingress.domain.repository.SampleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
class SampleService {

    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Transactional
    public void saveAll(List<Sample> samples) {
        log.info("Persist {} Samples", samples.size());
        sampleRepository.saveAll(samples);
    }
}
