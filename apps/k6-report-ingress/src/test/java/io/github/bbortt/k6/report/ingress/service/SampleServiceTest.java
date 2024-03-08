package io.github.bbortt.k6.report.ingress.service;

import io.github.bbortt.k6.report.ingress.domain.Sample;
import io.github.bbortt.k6.report.ingress.domain.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepositoryMock;

    private SampleService sampleService;

    @BeforeEach
    void setUp() {
        sampleService = new SampleService(sampleRepositoryMock);
    }

    @Test
    void isService() {
        assertThat(SampleService.class)
                .hasAnnotations(Service.class);
    }

    @Test
    void saveAllShouldCallRepository() {
        var samples = asList(new Sample(), new Sample());

        sampleService.saveAll(samples);

        verify(sampleRepositoryMock, times(1)).saveAll(samples);
    }
}
