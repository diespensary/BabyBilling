package com.example.brtservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Data
public class CdrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2, nullable = false)
    private String callType;

    @Column(nullable = false)
    private String firstSubscriber;

    @Column(nullable = false)
    private String secondSubscriber;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Длительность разговора в секундах **/
    @Column(nullable = false)
    private Long talkTimeSeconds;

    @Column(nullable = false)
    private Boolean processed = false;

    /**
     * Перед сохранением/обновлением вычисляем длительность.
     */
    @PrePersist
    @PreUpdate
    public void calculateTalkTime() {
        if (startTime != null && endTime != null) {
            this.talkTimeSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        }
    }
}
