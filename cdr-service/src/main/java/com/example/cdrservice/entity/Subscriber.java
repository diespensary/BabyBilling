package com.example.cdrservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String msisdn;

    @Column(nullable = false)
    private String operator;
}
