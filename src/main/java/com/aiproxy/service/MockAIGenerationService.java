package com.aiproxy.service;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;
import com.aiproxy.model.UsageRecord;
import com.aiproxy.repository.UsageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockAIGenerationService implements AIGenerationService {

    private static final long MOCK_LATENCY_MS = 1200L;
    private static final int PROMPT_SUMMARY_LIMIT = 600;

    private static final List<String> MOCK_RESPONSES = List.of(
        "Claro, aqui tienes una idea inicial para avanzar con tu proyecto.",
        "Te propongo este enfoque practico para resolver el problema paso a paso.",
        "Buena pregunta. Una estrategia robusta es separar validacion, dominio y persistencia.",
        "Podemos optimizar este flujo aplicando cache, control de errores y metricas.",
        "Si quieres, tambien puedo devolverte una version mas resumida y accionable."
    );

    private final UsageRepository usageRepository;

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        validateRequest(request);

        try {
            Thread.sleep(MOCK_LATENCY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            saveUsage(request, 0L, false);
            throw new IllegalStateException("Generation request interrupted", ex);
        }

        long tokensUsed = calculateTokensUsed(request.getTokensRequested());
        String generatedText = randomResponse();
        saveUsage(request, tokensUsed, true);

        return GenerationResponse.builder()
            .generatedText(generatedText)
            .tokensUsed(tokensUsed)
            .timestamp(LocalDateTime.now())
            .build();
    }

    private void validateRequest(GenerationRequest request) {
        if (request == null || request.getUserId() == null || request.getTokensRequested() == null) {
            throw new IllegalArgumentException("Invalid generation request");
        }

        if (request.getTokensRequested() <= 0) {
            throw new IllegalArgumentException("tokensRequested must be greater than zero");
        }
    }

    private long calculateTokensUsed(Long tokensRequested) {
        long min = Math.max(1L, Math.round(tokensRequested * 0.75d));
        return ThreadLocalRandom.current().nextLong(min, tokensRequested + 1);
    }

    private String randomResponse() {
        int index = ThreadLocalRandom.current().nextInt(MOCK_RESPONSES.size());
        return MOCK_RESPONSES.get(index);
    }

    private void saveUsage(GenerationRequest request, long tokensUsed, boolean success) {
        UsageRecord usageRecord = UsageRecord.builder()
            .userId(request.getUserId())
            .tokensUsed(tokensUsed)
            .prompt(summarizePrompt(request.getPrompt()))
            .success(success)
            .build();

        usageRepository.save(usageRecord);
    }

    private String summarizePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "No prompt provided";
        }

        String normalizedPrompt = prompt.trim().replaceAll("\\s+", " ");
        if (normalizedPrompt.length() <= PROMPT_SUMMARY_LIMIT) {
            return normalizedPrompt;
        }

        return normalizedPrompt.substring(0, PROMPT_SUMMARY_LIMIT - 3) + "...";
    }
}

