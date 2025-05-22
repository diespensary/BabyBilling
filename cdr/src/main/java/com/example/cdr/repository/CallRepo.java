package com.example.cdr.repository;

import com.example.demo.entity.CallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CallRepo extends JpaRepository<CallEntity, Long> {
    List<CallEntity> findByInitiating_IdOrReceiving_Id(long initId, long recId);

    long count();

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE call_data", nativeQuery = true)
    void truncateTable();

}
