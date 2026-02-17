package com.ecorent.gestionalquileres.dto.report;

public record TopEquipmentResponse(

        Long equipmentId,
        String equipmentName,
        Long totalRentals
) {}
