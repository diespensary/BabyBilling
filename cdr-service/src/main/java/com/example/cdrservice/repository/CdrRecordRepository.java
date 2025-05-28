package com.example.cdrservice.repository;

import com.example.cdrservice.entity.CdrRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CdrRecordRepository extends JpaRepository<CdrRecord, Long> {
    List<CdrRecord> findTop10ByOrderByStartTimeAsc();
}
