package com.aiproxy.repository;

import com.aiproxy.model.Quota;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotaRepository extends JpaRepository<Quota, Long> {

    Optional<Quota> findByUserId(Long userId);
}

