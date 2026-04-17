package com.aiproxy.controller;

import com.aiproxy.dto.DailyUsageDTO;
import com.aiproxy.dto.QuotaStatusResponse;
import com.aiproxy.dto.UpgradePlanRequest;
import com.aiproxy.service.QuotaManagementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
@RequiredArgsConstructor
public class QuotaController {

    private final QuotaManagementService quotaManagementService;

    @GetMapping("/status")
    public ResponseEntity<QuotaStatusResponse> getStatus(@RequestParam Long userId) {
        return ResponseEntity.ok(quotaManagementService.getQuotaStatus(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyUsageDTO>> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(quotaManagementService.getLast7DaysUsage(userId));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<QuotaStatusResponse> upgradePlan(@Valid @RequestBody UpgradePlanRequest request) {
        return ResponseEntity.ok(quotaManagementService.upgradePlan(request));
    }
}

