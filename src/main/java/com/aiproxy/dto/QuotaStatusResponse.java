package com.aiproxy.dto;

import com.aiproxy.model.Plan;
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
public class QuotaStatusResponse {

    private Long userId;
    private Plan plan;
    private Long tokensUsed;
    private Long tokensRemaining;
    private LocalDate resetDate;
}

