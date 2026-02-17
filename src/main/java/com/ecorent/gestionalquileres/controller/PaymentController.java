package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.payment.*;
import com.ecorent.gestionalquileres.entity.Payment;
import com.ecorent.gestionalquileres.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals/{rentalId}/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Gestión de pagos")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Registrar pago de un alquiler")
    @PostMapping
    public ResponseEntity<PaymentResponse> register(
            @PathVariable Long rentalId,
            @Valid @RequestBody PaymentRequest request) {

        Payment payment = paymentService.registerPayment(rentalId, request.amount());

        String status = paymentService.getPaymentStatus(rentalId);

        return ResponseEntity.ok(
                new PaymentResponse(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentDate(),
                        status
                )
        );
    }
}
