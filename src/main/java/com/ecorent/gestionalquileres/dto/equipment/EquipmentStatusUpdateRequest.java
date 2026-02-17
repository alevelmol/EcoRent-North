package com.ecorent.gestionalquileres.dto.equipment;

import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import jakarta.validation.constraints.NotNull;

public record EquipmentStatusUpdateRequest(

        @NotNull
        EquipmentStatus status
) {}
