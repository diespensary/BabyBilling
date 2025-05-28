package com.example.brtservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class TariffBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Float tariffBalance;

    @Column(nullable = false)
    private LocalDate dateLastBilling;

    /** Владелец баланса — абонент с periodic=true */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abonent_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tariff_balance_abonent"))
    private Abonent abonent;
}

