package com.aiproxy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long tokensUsed;

    @Column(nullable = false)
    private Long tokensLimit;

    @Column(nullable = false)
    private LocalDate resetDate;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (tokensUsed == null) {
            tokensUsed = 0L;
        }
        if (lastUpdated == null) {
            lastUpdated = now;
        }
        if (resetDate == null) {
            resetDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        }
    }

    @PreUpdate
    void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

