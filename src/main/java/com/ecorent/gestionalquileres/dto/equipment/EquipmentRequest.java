package com.ecorent.gestionalquileres.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record EquipmentRequest(

        @NotBlank
        String name,

        @NotBlank
        String category,

        @NotBlank
        String internalCode,

        @NotNull
        @Positive
        BigDecimal pricePerDay
) {}
