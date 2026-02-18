package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Payment;
import com.ecorent.gestionalquileres.entity.Rental;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.repository.PaymentRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;

    // RF-12 + RN-05
    public Payment registerPayment(Long rentalId, BigDecimal amount) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Alquiler no encontrado"));

        BigDecimal paid = paymentRepository.sumPaymentsByRentalId(rentalId);

        if (paid.add(amount).compareTo(rental.getTotalAmount()) > 0) {
            throw new BusinessException("El pago supera el importe pendiente");
        }

        Payment payment = Payment.builder()
                .rental(rental)
                .amount(amount)
                .paymentDate(LocalDate.now())
                .build();

        return paymentRepository.save(payment);
    }

    // RF-13
    public String getPaymentStatus(Long rentalId) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Alquiler no encontrado"));

        BigDecimal paid = paymentRepository.sumPaymentsByRentalId(rentalId);

        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            return "Pendiente";
        } else if (paid.compareTo(rental.getTotalAmount()) < 0) {
            return "Pagado parcial";
        } else {
            return "Pagado completo";
        }
    }
}
