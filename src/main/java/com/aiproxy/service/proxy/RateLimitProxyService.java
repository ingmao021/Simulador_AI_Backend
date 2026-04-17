package com.aiproxy.service.proxy;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;
import com.aiproxy.exception.RateLimitExceededException;
import com.aiproxy.model.Plan;
import com.aiproxy.model.User;
import com.aiproxy.repository.UserRepository;
import com.aiproxy.service.AIGenerationService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RateLimitProxyService implements AIGenerationService {

    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final AIGenerationService nextService;

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        validateRequest(request);

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        Plan plan = user.getPlan();
        Integer requestsPerMinuteLimit = plan.getRequestsPerMinuteLimit();
        if (plan.isUnlimited() || requestsPerMinuteLimit == null) {
            return nextService.generate(request);
        }

        RateLimitResult rateLimitResult = tryAcquireSlot(String.valueOf(user.getId()), requestsPerMinuteLimit);
        if (!rateLimitResult.allowed()) {
            throw new RateLimitExceededException(
                "Rate limit exceeded for plan " + plan + ". Please retry later.",
                rateLimitResult.retryAfterSeconds()
            );
        }

        return nextService.generate(request);
    }

    public void resetAllCounters() {
        requestCounters.clear();
    }

    private void validateRequest(GenerationRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("Invalid generation request");
        }
    }

    private RateLimitResult tryAcquireSlot(String userKey, int requestsPerMinuteLimit) {
        long now = System.currentTimeMillis();
        AtomicReference<RateLimitResult> resultRef = new AtomicReference<>();

        requestCounters.compute(userKey, (key, currentCounter) -> {
            if (currentCounter == null || isWindowExpired(currentCounter, now)) {
                resultRef.set(new RateLimitResult(true, 0L));
                return new RequestCounter(now, 1);
            }

            if (currentCounter.requestCount() >= requestsPerMinuteLimit) {
                long retryAfterSeconds = calculateRetryAfterSeconds(currentCounter.windowStartMillis(), now);
                resultRef.set(new RateLimitResult(false, retryAfterSeconds));
                return currentCounter;
            }

            resultRef.set(new RateLimitResult(true, 0L));
            return new RequestCounter(currentCounter.windowStartMillis(), currentCounter.requestCount() + 1);
        });

        return resultRef.get();
    }

    private boolean isWindowExpired(RequestCounter currentCounter, long now) {
        return now - currentCounter.windowStartMillis() >= WINDOW_MILLIS;
    }

    private long calculateRetryAfterSeconds(long windowStartMillis, long now) {
        long remainingMillis = WINDOW_MILLIS - (now - windowStartMillis);
        return Math.max(1L, (remainingMillis + 999L) / 1000L);
    }

    private record RequestCounter(long windowStartMillis, int requestCount) {
    }

    private record RateLimitResult(boolean allowed, long retryAfterSeconds) {
    }
}


