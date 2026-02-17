package com.ecorent.gestionalquileres.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(

        @NotBlank
        String name,

        @NotBlank
        String dni,

        @NotBlank
        String phone,

        @Email
        String email
) {}
