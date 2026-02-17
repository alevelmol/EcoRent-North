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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDate paymentDate;

    @ManyToOne(optional = false)
    private Rental rental;
}

