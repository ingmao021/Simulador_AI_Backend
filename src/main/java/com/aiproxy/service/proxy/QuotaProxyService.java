package com.aiproxy.service.proxy;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;
import com.aiproxy.exception.QuotaExceededException;
import com.aiproxy.model.Quota;
import com.aiproxy.repository.QuotaRepository;
import com.aiproxy.service.AIGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class QuotaProxyService implements AIGenerationService {

    private final QuotaRepository quotaRepository;
    private final AIGenerationService nextService;

    @Override
    @Transactional
    public GenerationResponse generate(GenerationRequest request) {
        validateRequest(request);

        Quota quota = quotaRepository.findByUserId(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Quota not found for user id: " + request.getUserId()));

        long availableTokens = Math.max(0L, quota.getTokensLimit() - quota.getTokensUsed());
        if (request.getTokensRequested() > availableTokens) {
            throw new QuotaExceededException(
                "Quota exceeded. Available tokens: " + availableTokens + ". Reset date: " + quota.getResetDate(),
                availableTokens,
                quota.getResetDate()
            );
        }

        GenerationResponse response = nextService.generate(request);

        long updatedTokensUsed = quota.getTokensUsed() + response.getTokensUsed();
        if (updatedTokensUsed > quota.getTokensLimit()) {
            long recalculatedAvailable = Math.max(0L, quota.getTokensLimit() - quota.getTokensUsed());
            throw new QuotaExceededException(
                "Quota exceeded after generation. Available tokens: " + recalculatedAvailable
                    + ". Reset date: " + quota.getResetDate(),
                recalculatedAvailable,
                quota.getResetDate()
            );
        }

        quota.setTokensUsed(updatedTokensUsed);
        quotaRepository.save(quota);

        return response;
    }

    private void validateRequest(GenerationRequest request) {
        if (request == null || request.getUserId() == null || request.getTokensRequested() == null) {
            throw new IllegalArgumentException("Invalid generation request");
        }

        if (request.getTokensRequested() <= 0) {
            throw new IllegalArgumentException("tokensRequested must be greater than zero");
        }
    }
}


