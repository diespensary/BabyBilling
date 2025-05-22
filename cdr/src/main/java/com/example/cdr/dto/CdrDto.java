package com.example.cdr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CdrDto {
    String flag;
    String initiator;
    String receiver;
    LocalDateTime startDate;
    LocalDateTime endDate;

}
