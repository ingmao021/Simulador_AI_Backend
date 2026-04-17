package com.aiproxy.service;

import com.aiproxy.dto.DailyUsageDTO;
import com.aiproxy.dto.QuotaStatusResponse;
import com.aiproxy.dto.UpgradePlanRequest;
import com.aiproxy.exception.UserNotFoundException;
import com.aiproxy.model.Plan;
import com.aiproxy.model.Quota;
import com.aiproxy.model.UsageRecord;
import com.aiproxy.model.User;
import com.aiproxy.repository.QuotaRepository;
import com.aiproxy.repository.UsageRepository;
import com.aiproxy.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuotaManagementService {

    private static final long ENTERPRISE_TOKEN_LIMIT = Long.MAX_VALUE;

    private final UserRepository userRepository;
    private final QuotaRepository quotaRepository;
    private final UsageRepository usageRepository;

    @Transactional(readOnly = true)
    public QuotaStatusResponse getQuotaStatus(Long userId) {
        User user = findUser(userId);
        Quota quota = findQuota(userId);

        long remainingTokens;
        if (user.getPlan().isUnlimited()) {
            remainingTokens = ENTERPRISE_TOKEN_LIMIT;
        } else {
            remainingTokens = Math.max(0L, quota.getTokensLimit() - quota.getTokensUsed());
        }

        return QuotaStatusResponse.builder()
            .userId(user.getId())
            .plan(user.getPlan())
            .tokensUsed(quota.getTokensUsed())
            .tokensRemaining(remainingTokens)
            .resetDate(quota.getResetDate())
            .build();
    }

    @Transactional(readOnly = true)
    public List<DailyUsageDTO> getLast7DaysUsage(Long userId) {
        findUser(userId);

        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.minusDays(6).atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        List<UsageRecord> usageRecords = usageRepository.findByUserIdAndRequestDateBetweenOrderByRequestDateDesc(
            userId,
            startDate,
            endDate
        );

        Map<LocalDate, List<UsageRecord>> recordsByDate = usageRecords.stream()
            .collect(Collectors.groupingBy(record -> record.getRequestDate().toLocalDate()));

        List<DailyUsageDTO> dailyUsage = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<UsageRecord> dayRecords = recordsByDate.getOrDefault(date, List.of());
            long tokensUsed = dayRecords.stream().mapToLong(UsageRecord::getTokensUsed).sum();

            dailyUsage.add(DailyUsageDTO.builder()
                .date(date)
                .tokensUsed(tokensUsed)
                .requestsCount((long) dayRecords.size())
                .build());
        }

        return dailyUsage.stream()
            .sorted(Comparator.comparing(DailyUsageDTO::getDate))
            .toList();
    }

    @Transactional
    public QuotaStatusResponse upgradePlan(UpgradePlanRequest request) {
        if (request == null || request.getUserId() == null || request.getNewPlan() == null) {
            throw new IllegalArgumentException("Invalid upgrade request");
        }

        User user = findUser(request.getUserId());
        Quota quota = findQuota(request.getUserId());

        user.setPlan(request.getNewPlan());
        userRepository.save(user);

        long newTokenLimit = resolveTokenLimit(request.getNewPlan());
        quota.setTokensLimit(newTokenLimit);
        if (quota.getTokensUsed() > newTokenLimit) {
            quota.setTokensUsed(newTokenLimit);
        }
        quotaRepository.save(quota);

        return getQuotaStatus(user.getId());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private Quota findQuota(Long userId) {
        return quotaRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Quota not found for user id: " + userId));
    }

    private long resolveTokenLimit(Plan plan) {
        if (plan.isUnlimited()) {
            return ENTERPRISE_TOKEN_LIMIT;
        }

        if (plan.getMonthlyTokenLimit() == null) {
            throw new IllegalArgumentException("Invalid token limit for plan: " + plan);
        }

        return plan.getMonthlyTokenLimit();
    }
}

