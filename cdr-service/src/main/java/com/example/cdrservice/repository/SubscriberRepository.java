package com.example.cdrservice.repository;

import com.example.cdrservice.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> { }