package com.aiproxy.dto;

import java.time.LocalDateTime;
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
public class GenerationResponse {

    private String generatedText;
    private Long tokensUsed;
    private LocalDateTime timestamp;
}

