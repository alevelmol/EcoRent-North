package com.ecorent.gestionalquileres.repository;

import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // RF-01 → código interno único
    Optional<Equipment> findByInternalCode(String internalCode);

    // RF-03 → filtrar por estado
    List<Equipment> findByStatus(EquipmentStatus status);

    // RF-14 → equipos más alquilados
    @Query("""
        SELECT e FROM Equipment e
        LEFT JOIN Rental r ON r.equipment = e
        GROUP BY e
        ORDER BY COUNT(r.id) DESC
    """)
    List<Equipment> findTopRentedEquipments();
}
