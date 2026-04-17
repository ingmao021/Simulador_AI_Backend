package com.aiproxy.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;
import com.aiproxy.exception.RateLimitExceededException;
import com.aiproxy.model.Plan;
import com.aiproxy.model.User;
import com.aiproxy.repository.UserRepository;
import com.aiproxy.service.AIGenerationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimitProxyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIGenerationService nextService;

    private RateLimitProxyService rateLimitProxyService;

    @BeforeEach
    void setUp() {
        rateLimitProxyService = new RateLimitProxyService(userRepository, nextService);
    }

    @Test
    void shouldAllowRequestWhenWithinPlanLimit() {
        GenerationRequest request = GenerationRequest.builder()
            .userId(1L)
            .prompt("Test prompt")
            .tokensRequested(100L)
            .build();

        User user = User.builder()
            .id(1L)
            .username("free_user")
            .email("free.user@aiproxy.com")
            .plan(Plan.FREE)
            .createdAt(LocalDateTime.now())
            .build();

        GenerationResponse expectedResponse = GenerationResponse.builder()
            .generatedText("ok")
            .tokensUsed(90L)
            .timestamp(LocalDateTime.now())
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(nextService.generate(any(GenerationRequest.class))).thenReturn(expectedResponse);

        GenerationResponse response = rateLimitProxyService.generate(request);

        assertEquals(expectedResponse.getGeneratedText(), response.getGeneratedText());
        verify(nextService, times(1)).generate(request);
    }

    @Test
    void shouldThrowWhenRateLimitIsExceeded() {
        GenerationRequest request = GenerationRequest.builder()
            .userId(2L)
            .prompt("Load test")
            .tokensRequested(50L)
            .build();

        User user = User.builder()
            .id(2L)
            .username("free_user_2")
            .email("free2@aiproxy.com")
            .plan(Plan.FREE)
            .createdAt(LocalDateTime.now())
            .build();

        GenerationResponse response = GenerationResponse.builder()
            .generatedText("ok")
            .tokensUsed(50L)
            .timestamp(LocalDateTime.now())
            .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(nextService.generate(any(GenerationRequest.class))).thenReturn(response);

        for (int i = 0; i < 10; i++) {
            rateLimitProxyService.generate(request);
        }

        RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> rateLimitProxyService.generate(request)
        );

        assertTrue(exception.getRetryAfterSeconds() > 0);
        verify(nextService, times(10)).generate(request);
    }
}

