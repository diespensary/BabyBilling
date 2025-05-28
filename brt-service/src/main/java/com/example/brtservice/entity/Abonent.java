package com.example.brtservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Abonent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String abonentName;

    @Column(length = 20, nullable = false)
    private String msisdn;

    @Column(precision = 5, scale = 1, nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private LocalDate dateRegistration;

    @Column(nullable = false)
    private Integer tariffId;

    /** Баланс тарифа, если periodic = true */
    @OneToOne(mappedBy = "abonent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private TariffBalance tariffBalance;
}
