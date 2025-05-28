package com.example.brtservice.parser;

import com.example.brtservice.dto.CdrRecordDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CdrFileParser {

    /** Разбирает весь CSV-пакет в список DTO */
    public static List<CdrRecordDto> parse(String csvPayload) {
        return Arrays.stream(csvPayload.split("\n"))
                .filter(line -> !line.isBlank())
                .map(CdrFileParser::parseLine)
                .collect(Collectors.toList());
    }

    /** Разбирает одну строку CSV в DTO */
    private static CdrRecordDto parseLine(String line) {
        String[] p = line.split(",");
        CdrRecordDto dto = new CdrRecordDto();
        dto.setCallType(p[0].trim());
        dto.setFirstSubscriber(p[1].trim());
        dto.setSecondSubscriber(p[2].trim());
        dto.setStartTime(LocalDateTime.parse(p[3].trim()));
        dto.setEndTime(LocalDateTime.parse(p[4].trim()));
        return dto;
    }
}
