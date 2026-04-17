package com.aiproxy.controller;

import com.aiproxy.dto.GenerationRequest;
import com.aiproxy.dto.GenerationResponse;
import com.aiproxy.service.AIGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIGenerationController {

    private final AIGenerationService aiGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(@Valid @RequestBody GenerationRequest request) {
        return ResponseEntity.ok(aiGenerationService.generate(request));
    }
}

