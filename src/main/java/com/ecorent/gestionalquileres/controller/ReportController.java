package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.report.*;
import com.ecorent.gestionalquileres.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reportes estratégicos")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Ingresos por periodo")
    @GetMapping("/income")
    public ResponseEntity<IncomeReportResponse> income(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        return ResponseEntity.ok(
                new IncomeReportResponse(
                        start,
                        end,
                        BigDecimal.valueOf(reportService.getIncomeBetween(start, end)) 
                )
        );
    }

    @Operation(summary = "Equipos más alquilados")
    @GetMapping("/top-equipments")
    public ResponseEntity<?> topEquipments() {
        return ResponseEntity.ok(reportService.getTopRentedEquipments());
    }

    @Operation(summary = "Clientes recurrentes")
    @GetMapping("/top-clients")
    public ResponseEntity<?> topClients() {
        return ResponseEntity.ok(reportService.getTopClients());
    }
}
