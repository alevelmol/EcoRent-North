package com.ecorent.gestionalquileres.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(

        Long id,
        BigDecimal amount,
        LocalDate paymentDate,
        String paymentStatus
) {}
