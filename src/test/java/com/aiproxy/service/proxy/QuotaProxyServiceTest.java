package com.aiproxy.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;
import com.aiproxy.exception.QuotaExceededException;
import com.aiproxy.model.Quota;
import com.aiproxy.repository.QuotaRepository;
import com.aiproxy.service.AIGenerationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuotaProxyServiceTest {

    @Mock
    private QuotaRepository quotaRepository;

    @Mock
    private AIGenerationService nextService;

    private QuotaProxyService quotaProxyService;

    @BeforeEach
    void setUp() {
        quotaProxyService = new QuotaProxyService(quotaRepository, nextService);
    }

    @Test
    void shouldAllowRequestWhenQuotaIsAvailable() {
        GenerationRequest request = GenerationRequest.builder()
            .userId(1L)
            .prompt("Generate content")
            .tokensRequested(120L)
            .build();

        Quota quota = Quota.builder()
            .id(1L)
            .userId(1L)
            .tokensUsed(100L)
            .tokensLimit(1000L)
            .resetDate(LocalDate.now().plusMonths(1).withDayOfMonth(1))
            .lastUpdated(LocalDateTime.now())
            .build();

        GenerationResponse aiResponse = GenerationResponse.builder()
            .generatedText("Generated text")
            .tokensUsed(80L)
            .timestamp(LocalDateTime.now())
            .build();

        when(quotaRepository.findByUserId(1L)).thenReturn(Optional.of(quota));
        when(nextService.generate(any(GenerationRequest.class))).thenReturn(aiResponse);

        GenerationResponse result = quotaProxyService.generate(request);

        assertEquals("Generated text", result.getGeneratedText());
        assertEquals(180L, quota.getTokensUsed());
        verify(quotaRepository).save(quota);
    }

    @Test
    void shouldThrowWhenQuotaIsExceeded() {
        GenerationRequest request = GenerationRequest.builder()
            .userId(2L)
            .prompt("Generate content")
            .tokensRequested(100L)
            .build();

        Quota quota = Quota.builder()
            .id(2L)
            .userId(2L)
            .tokensUsed(950L)
            .tokensLimit(1000L)
            .resetDate(LocalDate.now().plusMonths(1).withDayOfMonth(1))
            .lastUpdated(LocalDateTime.now())
            .build();

        when(quotaRepository.findByUserId(2L)).thenReturn(Optional.of(quota));

        assertThrows(QuotaExceededException.class, () -> quotaProxyService.generate(request));

        verify(nextService, never()).generate(any(GenerationRequest.class));
        verify(quotaRepository, never()).save(any(Quota.class));
    }
}

