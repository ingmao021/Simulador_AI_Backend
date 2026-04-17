package com.aiproxy.config;

import com.aiproxy.repository.QuotaRepository;
import com.aiproxy.repository.UserRepository;
import com.aiproxy.service.AIGenerationService;
import com.aiproxy.service.MockAIGenerationService;
import com.aiproxy.service.proxy.QuotaProxyService;
import com.aiproxy.service.proxy.RateLimitProxyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProxyChainConfig {

    @Bean
    public QuotaProxyService quotaProxyService(
        QuotaRepository quotaRepository,
        MockAIGenerationService mockAIGenerationService
    ) {
        return new QuotaProxyService(quotaRepository, mockAIGenerationService);
    }

    @Bean
    public RateLimitProxyService rateLimitProxyService(
        UserRepository userRepository,
        QuotaProxyService quotaProxyService
    ) {
        return new RateLimitProxyService(userRepository, quotaProxyService);
    }

    @Bean
    @Primary
    public AIGenerationService aiGenerationService(RateLimitProxyService rateLimitProxyService) {
        return rateLimitProxyService;
    }
}


