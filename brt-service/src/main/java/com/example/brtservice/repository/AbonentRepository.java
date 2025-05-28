package com.example.brtservice.repository;

import com.example.brtservice.entity.Abonent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbonentRepository extends JpaRepository<Abonent, Integer> {
    boolean existsByMsisdn(String msisnd);

    Optional<Abonent> findByMsisdn(String msisdn);
}
