package com.aiproxy.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class QuotaExceededException extends RuntimeException {

    private final long tokensAvailable;
    private final LocalDate resetDate;

    public QuotaExceededException(String message, long tokensAvailable, LocalDate resetDate) {
        super(message);
        this.tokensAvailable = tokensAvailable;
        this.resetDate = resetDate;
    }

    public long getTokensAvailable() {
        return tokensAvailable;
    }

    public LocalDate getResetDate() {
        return resetDate;
    }
}

