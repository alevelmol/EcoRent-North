package com.ecorent.gestionalquileres.dto.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RentalRequest(

        @NotBlank
        String clientDni,

        @NotNull
        Long equipmentId,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {}
