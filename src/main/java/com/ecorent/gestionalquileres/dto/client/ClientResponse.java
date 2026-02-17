package com.ecorent.gestionalquileres.dto.client;

public record ClientResponse(

        Long id,
        String name,
        String dni,
        String phone,
        String email
) {}
