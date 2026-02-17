package com.ecorent.gestionalquileres.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.math.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal totalAmount;

    private boolean returned = false;

    @ManyToOne(optional = false)
    private Equipment equipment;

    @ManyToOne(optional = false)
    private Client client;
}

