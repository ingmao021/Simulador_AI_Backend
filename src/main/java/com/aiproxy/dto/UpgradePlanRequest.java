package com.aiproxy.dto;

import com.aiproxy.model.Plan;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class UpgradePlanRequest {

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    private Plan newPlan;
}

