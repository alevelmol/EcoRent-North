package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.rental.*;
import com.ecorent.gestionalquileres.entity.Rental;
import com.ecorent.gestionalquileres.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@Tag(name = "Rentals", description = "Gestión de alquileres")
public class RentalController {

    private final RentalService rentalService;

    @Operation(summary = "Crear nuevo alquiler")
    @PostMapping
    public ResponseEntity<RentalResponse> create(
            @Valid @RequestBody RentalRequest request) {

        Rental rental = rentalService.createRental(
                request.clientDni(),
                request.equipmentId(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.ok(
                new RentalResponse(
                        rental.getId(),
                        rental.getClient().getName(),
                        rental.getClient().getDni(),
                        rental.getEquipment().getId(),
                        rental.getEquipment().getName(),
                        rental.getStartDate(),
                        rental.getEndDate(),
                        rental.getTotalAmount(),
                        rental.isReturned()
                )
        );
    }

    @Operation(summary = "Registrar devolución del equipo")
    @PutMapping("/{id}/return")
    public ResponseEntity<RentalReturnResponse> registerReturn(
            @PathVariable Long id) {

        Rental rental = rentalService.registerReturn(id);

        return ResponseEntity.ok(
                new RentalReturnResponse(
                        rental.getId(),
                        rental.isReturned(),
                        rental.getEquipment().getStatus().name()
                )
        );
    }
    @GetMapping("/{dni}/rentals")
    public ResponseEntity<List<Rental>> getClientHistory(@PathVariable String dni) {
        return ResponseEntity.ok(rentalService.getClientHistory(dni));
    }

}
