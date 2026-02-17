package com.ecorent.gestionalquileres.dto.report;

public record TopClientResponse(

        Long clientId,
        String clientName,
        String dni,
        Long totalRentals
) {}
