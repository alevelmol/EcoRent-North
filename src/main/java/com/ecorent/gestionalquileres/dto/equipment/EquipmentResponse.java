package com.ecorent.gestionalquileres.dto.equipment;

import com.ecorent.gestionalquileres.entity.EquipmentStatus;

import java.math.BigDecimal;

public record EquipmentResponse(

        Long id,
        String name,
        String category,
        String internalCode,
        BigDecimal pricePerDay,
        EquipmentStatus status
) {}
