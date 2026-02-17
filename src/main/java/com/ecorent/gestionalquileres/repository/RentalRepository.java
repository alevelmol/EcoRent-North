package com.ecorent.gestionalquileres.repository;

import com.ecorent.gestionalquileres.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    // ✅ RN-01 Validación de solapamiento
    List<Rental> findByEquipmentIdAndReturnedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long equipmentId,
            LocalDate endDate,
            LocalDate startDate
    );

    // ✅ RF-06 Historial por cliente
    List<Rental> findByClientId(Long clientId);

    // ✅ RN-04 Alquileres activos
    List<Rental> findByReturnedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate today1,
            LocalDate today2
    );

    // ✅ RF-14 Ingresos por periodo
    @Query("""
        SELECT COALESCE(SUM(r.totalAmount), 0)
        FROM Rental r
        WHERE r.startDate >= :start
        AND r.endDate <= :end
    """)
    Double calculateIncomeBetween(LocalDate start, LocalDate end);
    
    List<Rental> findByClientDni(String dni);
}