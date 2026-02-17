package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.client.*;
import com.ecorent.gestionalquileres.dto.rental.RentalResponse;
import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.service.ClientService;
import com.ecorent.gestionalquileres.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestión de clientes")
public class ClientController {

    private final ClientService clientService;
    private final RentalService rentalService;

    @Operation(summary = "Registrar nuevo cliente")
    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @Valid @RequestBody ClientRequest request) {

        Client client = new Client();
        client.setName(request.name());
        client.setDni(request.dni());
        client.setPhone(request.phone());
        client.setEmail(request.email());

        Client saved = clientService.createClient(client);

        return ResponseEntity.ok(
                new ClientResponse(
                        saved.getId(),
                        saved.getName(),
                        saved.getDni(),
                        saved.getPhone(),
                        saved.getEmail()
                )
        );
    }

    @Operation(summary = "Consultar historial de alquileres por DNI")
    @GetMapping("/{dni}/rentals")
    public ResponseEntity<List<RentalResponse>> getHistory(
            @PathVariable String dni) {

        List<RentalResponse> rentals = rentalService.getClientHistory(dni)
                .stream()
                .map(r -> new RentalResponse(
                        r.getId(),
                        r.getClient().getName(),
                        r.getClient().getDni(),
                        r.getEquipment().getId(),
                        r.getEquipment().getName(),
                        r.getStartDate(),
                        r.getEndDate(),
                        r.getTotalAmount(),
                        r.isReturned()
                ))
                .toList();

        return ResponseEntity.ok(rentals);
    }
}
