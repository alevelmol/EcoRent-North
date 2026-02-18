package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Payment;
import com.ecorent.gestionalquileres.entity.Rental;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.PaymentRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    RentalRepository rentalRepository;

    @InjectMocks
    PaymentService paymentService;

    // ---------- registerPayment ----------

    @Test
    void registerPayment_whenRentalNotFound_throwsBusinessException() {
        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> paymentService.registerPayment(1L, new BigDecimal("10"))
        );

        assertEquals("Alquiler no encontrado", ex.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void registerPayment_whenPaymentExceedsTotal_throwsBusinessException() {
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(new BigDecimal("90"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> paymentService.registerPayment(1L, new BigDecimal("20"))
        );

        assertEquals("El pago supera el importe pendiente", ex.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void registerPayment_whenWithinRemainingAmount_savesPayment() {
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(new BigDecimal("30"));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Payment.class));

        BigDecimal amount = new BigDecimal("40");
        LocalDate today = LocalDate.now();

        Payment result = paymentService.registerPayment(1L, amount);

        assertNotNull(result);
        assertEquals(rental, result.getRental());
        assertEquals(amount, result.getAmount());
        assertEquals(today, result.getPaymentDate());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void registerPayment_whenAmountCompletesTotal_savesPayment() {
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(new BigDecimal("60"));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Payment.class));

        BigDecimal amount = new BigDecimal("40");

        Payment result = paymentService.registerPayment(1L, amount);

        assertEquals(new BigDecimal("40"), result.getAmount());
        assertEquals(rental, result.getRental());
        verify(paymentRepository).save(any(Payment.class));
    }

    // ---------- getPaymentStatus ----------

    @Test
    void getPaymentStatus_whenRentalNotFound_throwsBusinessException() {
        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> paymentService.getPaymentStatus(1L)
        );

        assertEquals("Alquiler no encontrado", ex.getMessage());
    }

    @Test
    void getPaymentStatus_whenNoPayments_returnsPendiente() {
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(BigDecimal.ZERO);

        String status = paymentService.getPaymentStatus(1L);

        assertEquals("Pendiente", status);
    }

    @Test
    void getPaymentStatus_whenPartialPayments_returnsPagadoParcial() {
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(new BigDecimal("40"));

        String status = paymentService.getPaymentStatus(1L);

        assertEquals("Pagado parcial", status);
    }

    @Test
    void getPaymentStatus_whenFullyPaid_returnsPagadoCompleto() {
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(new BigDecimal("100"));

        String status = paymentService.getPaymentStatus(1L);

        assertEquals("Pagado completo", status);
    }

    @Test
    void getPaymentStatus_whenOverPaidStillReturnsPagadoCompleto() {
        // Aunque por RN-05 no debería ocurrir, cubrimos la rama del else
        Rental rental = Rental.builder()
                .id(1L)
                .totalAmount(new BigDecimal("100"))
                .build();

        when(rentalRepository.findById(1L)).thenReturn(java.util.Optional.of(rental));
        when(paymentRepository.sumPaymentsByRentalId(1L)).thenReturn(new BigDecimal("150"));

        String status = paymentService.getPaymentStatus(1L);

        assertEquals("Pagado completo", status);
    }
}
