package com.aiproxy.service;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;

public interface AIGenerationService {

    GenerationResponse generate(GenerationRequest request);
}

