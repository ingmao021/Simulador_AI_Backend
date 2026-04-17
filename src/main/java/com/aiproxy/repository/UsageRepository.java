package com.aiproxy.repository;

import com.aiproxy.model.UsageRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findByUserIdAndRequestDateBetweenOrderByRequestDateDesc(
        Long userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}

