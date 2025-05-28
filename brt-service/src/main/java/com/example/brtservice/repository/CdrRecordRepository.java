package com.example.brtservice.repository;

import com.example.brtservice.entity.CdrRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CdrRecordRepository extends JpaRepository<CdrRecord, Long> {
    List<CdrRecord> findTop10ByCallTypeAndProcessedFalseOrderByStartTimeAsc(String callType);
}
