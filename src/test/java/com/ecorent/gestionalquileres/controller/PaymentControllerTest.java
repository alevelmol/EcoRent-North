package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.payment.PaymentRequest;
import com.ecorent.gestionalquileres.entity.Payment;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PaymentService paymentService;

    // ---------- POST /api/rentals/{rentalId}/payments ----------

    @Test
    void registerPayment_returnsPaymentResponse_whenValidRequest() throws Exception {
        Long rentalId = 10L;
        PaymentRequest request = new PaymentRequest(new BigDecimal("50"));

        Payment payment = Payment.builder()
                .id(1L)
                .amount(new BigDecimal("50"))
                .paymentDate(LocalDate.of(2024, 1, 10))
                .build();

        given(paymentService.registerPayment(eq(rentalId), eq(new BigDecimal("50"))))
                .willReturn(payment);
        given(paymentService.getPaymentStatus(rentalId))
                .willReturn("Pagado parcial");

        mockMvc.perform(post("/api/rentals/{rentalId}/payments", rentalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.paymentStatus").value("Pagado parcial"))
                .andExpect(jsonPath("$.paymentDate").value("2024-01-10"));
    }

    @Test
    void registerPayment_returnsNotFound_whenRentalNotFound() throws Exception {
        Long rentalId = 99L;
        PaymentRequest request = new PaymentRequest(new BigDecimal("50"));

        given(paymentService.registerPayment(eq(rentalId), any(BigDecimal.class)))
                .willThrow(new NotFoundException("Alquiler no encontrado"));

        mockMvc.perform(post("/api/rentals/{rentalId}/payments", rentalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Alquiler no encontrado"));
    }

    @Test
    void registerPayment_returnsBadRequest_whenPaymentExceedsTotal() throws Exception {
        Long rentalId = 10L;
        PaymentRequest request = new PaymentRequest(new BigDecimal("200"));

        given(paymentService.registerPayment(eq(rentalId), any(BigDecimal.class)))
                .willThrow(new BusinessException("El pago supera el importe pendiente"));

        mockMvc.perform(post("/api/rentals/{rentalId}/payments", rentalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("El pago supera el importe pendiente"));
    }

    @Test
    void registerPayment_returnsBadRequest_whenValidationFails() throws Exception {
        // amount nulo o no positivo
        String invalidJson = """
                {
                  "amount": 0
                }
                """;

        mockMvc.perform(post("/api/rentals/10/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }
}
