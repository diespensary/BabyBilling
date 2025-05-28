package com.example.brtservice.repository;

import com.example.brtservice.entity.TariffBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TariffBalanceRepository extends JpaRepository<TariffBalance, Integer> {
    Optional<TariffBalance> findByAbonentId(Integer abonentId);
}
