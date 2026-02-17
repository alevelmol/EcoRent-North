package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.*;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {

    private final RentalRepository rentalRepository;
    private final EquipmentRepository equipmentRepository;
    private final ClientRepository clientRepository;

    // RF-07 + RN-01 + RN-02 + RN-03
    public Rental createRental(String clientDni, Long equipmentId,
                               LocalDate start, LocalDate end) {

        if (end.isBefore(start)) {
            throw new BusinessException("Fecha fin no puede ser anterior a fecha inicio");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException("Equipo no encontrado"));

        if (equipment.getStatus() == EquipmentStatus.MAINTENANCE) {
            throw new BusinessException("Equipo en mantenimiento");
        }

        // RN-01 Solapamiento
        List<Rental> overlapping =
                rentalRepository.findByEquipmentIdAndReturnedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        equipmentId, end, start);

        if (!overlapping.isEmpty()) {
            throw new BusinessException("Existe solapamiento de fechas");
        }

        Client client = clientRepository.findByDni(clientDni)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));

        long days = ChronoUnit.DAYS.between(start, end) + 1;

        BigDecimal total = equipment.getPricePerDay()
                .multiply(BigDecimal.valueOf(days));

        Rental rental = Rental.builder()
                .client(client)
                .equipment(equipment)
                .startDate(start)
                .endDate(end)
                .totalAmount(total)
                .returned(false)
                .build();

        equipment.setStatus(EquipmentStatus.RENTED);

        return rentalRepository.save(rental);
    }

    // RF-11
    public Rental registerReturn(Long rentalId) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException("Alquiler no encontrado"));

        rental.setReturned(true);

        rental.getEquipment().setStatus(EquipmentStatus.AVAILABLE);

        return rental;
    }

    // RF-06
    public List<Rental> getClientHistory(String dni) {
        return rentalRepository.findByClientDni(dni);
    }
}

