package com.example.cdr.repository;

import com.example.demo.entity.SubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubsRepo extends JpaRepository<SubscriberEntity, Long> {

    Optional<SubscriberEntity> findByMsisdn(String number);
}
