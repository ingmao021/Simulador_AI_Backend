package com.aiproxy.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyUsageDTO {

    private LocalDate date;
    private Long tokensUsed;
    private Long requestsCount;
}

