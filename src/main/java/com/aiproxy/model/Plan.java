package com.aiproxy.model;

public enum Plan {
    FREE(10, 50_000L),
    PRO(60, 500_000L),
    ENTERPRISE(null, null);

    private final Integer requestsPerMinuteLimit;
    private final Long monthlyTokenLimit;

    Plan(Integer requestsPerMinuteLimit, Long monthlyTokenLimit) {
        this.requestsPerMinuteLimit = requestsPerMinuteLimit;
        this.monthlyTokenLimit = monthlyTokenLimit;
    }

    public Integer getRequestsPerMinuteLimit() {
        return requestsPerMinuteLimit;
    }

    public Long getMonthlyTokenLimit() {
        return monthlyTokenLimit;
    }

    public boolean isUnlimited() {
        return requestsPerMinuteLimit == null && monthlyTokenLimit == null;
    }
}

