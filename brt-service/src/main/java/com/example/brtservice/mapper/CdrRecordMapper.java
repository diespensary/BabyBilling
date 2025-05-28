package com.example.brtservice.mapper;

import com.example.brtservice.dto.CdrRecordDto;
import com.example.brtservice.entity.CdrRecord;

public class CdrRecordMapper {

    public static CdrRecord toEntity(CdrRecordDto dto) {
        CdrRecord r = new CdrRecord();
        r.setCallType(dto.getCallType());
        r.setFirstSubscriber(dto.getFirstSubscriber());
        r.setSecondSubscriber(dto.getSecondSubscriber());
        r.setStartTime(dto.getStartTime());
        r.setEndTime(dto.getEndTime());
        // talkTimeSeconds посчитается в @PrePersist
        return r;
    }
}
