package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.equipment.*;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
@Tag(name = "Equipments", description = "Gestión de equipos")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "Registrar nuevo equipo")
    @PostMapping
    public ResponseEntity<EquipmentResponse> create(
            @Valid @RequestBody EquipmentRequest request) {

        Equipment equipment = new Equipment();
        equipment.setName(request.name());
        equipment.setCategory(request.category());
        equipment.setInternalCode(request.internalCode());
        equipment.setPricePerDay(request.pricePerDay());

        Equipment saved = equipmentService.createEquipment(equipment);

        return ResponseEntity.ok(toResponse(saved));
    }

    @Operation(summary = "Listar todos los equipos")
    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> findAll() {

        List<EquipmentResponse> response = equipmentService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Equipment> update(@PathVariable Long id,
                                            @RequestBody Equipment equipment) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cambiar estado del equipo")
    @PutMapping("/{id}/status")
    public ResponseEntity<EquipmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentStatusUpdateRequest request) {

        Equipment updated = equipmentService.changeStatus(id, request.status());

        return ResponseEntity.ok(toResponse(updated));
    }

    private EquipmentResponse toResponse(Equipment e) {
        return new EquipmentResponse(
                e.getId(),
                e.getName(),
                e.getCategory(),
                e.getInternalCode(),
                e.getPricePerDay(),
                e.getStatus()
        );
    }
}
