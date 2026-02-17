package com.ecorent.gestionalquileres.dto.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalResponse(

        Long id,
        String clientName,
        String clientDni,
        Long equipmentId,
        String equipmentName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalAmount,
        boolean returned
) {}
