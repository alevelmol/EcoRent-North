package com.ecorent.gestionalquileres.repository;

import com.ecorent.gestionalquileres.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByRentalId(Long rentalId);

    // ✅ RN-05 → suma de pagos
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.rental.id = :rentalId
    """)
    BigDecimal sumPaymentsByRentalId(Long rentalId);
}