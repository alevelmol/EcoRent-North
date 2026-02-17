package com.ecorent.gestionalquileres.dto.rental;

public record RentalReturnResponse(

        Long rentalId,
        boolean returned,
        String equipmentStatus
) {}
