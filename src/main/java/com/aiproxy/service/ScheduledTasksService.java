package com.aiproxy.service;

import com.aiproxy.model.Quota;
import com.aiproxy.repository.QuotaRepository;
import com.aiproxy.service.proxy.RateLimitProxyService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final RateLimitProxyService rateLimitProxyService;
    private final QuotaRepository quotaRepository;

    @Scheduled(fixedRate = 60000)
    public void resetRateLimits() {
        rateLimitProxyService.resetAllCounters();
        log.debug("Rate limit counters reset completed.");
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void resetMonthlyQuotas() {
        List<Quota> quotas = quotaRepository.findAll();
        LocalDate nextResetDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        for (Quota quota : quotas) {
            quota.setTokensUsed(0L);
            quota.setResetDate(nextResetDate);
        }

        quotaRepository.saveAll(quotas);
        log.info("Monthly quotas reset completed for {} users. Next reset date: {}", quotas.size(), nextResetDate);
    }
}

