package com.example.brtservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CdrRecordDto {
    private String callType;
    private String firstSubscriber;
    private String secondSubscriber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
