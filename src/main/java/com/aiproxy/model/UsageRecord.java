package com.aiproxy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usage_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long tokensUsed;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false, length = 600)
    private String prompt;

    @Column(nullable = false)
    private boolean success;

    @PrePersist
    void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
    }
}

