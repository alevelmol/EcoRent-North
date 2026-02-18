package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.repository.EquipmentRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RentalRepository rentalRepository;

    // RF-01
    public Equipment createEquipment(Equipment equipment) {

        if (equipmentRepository.findByInternalCode(equipment.getInternalCode()).isPresent()) {
            throw new BusinessException("Ya existe un equipo con ese código interno");
        }

        equipment.setStatus(EquipmentStatus.AVAILABLE);

        return equipmentRepository.save(equipment);
    }

 // RF-02 con restricciones
    public Equipment updateEquipment(Long id, Equipment updated) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));

        if (equipment.getStatus() == EquipmentStatus.RENTED) {
            throw new BusinessException("No se puede modificar un equipo alquilado");
        }

        equipment.setName(updated.getName());
        equipment.setCategory(updated.getCategory());
        equipment.setPricePerDay(updated.getPricePerDay());

        return equipment;
    }

    public void deleteEquipment(Long id) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));

        if (equipment.getStatus() == EquipmentStatus.RENTED) {
            throw new BusinessException("No se puede eliminar un equipo alquilado");
        }

        boolean hasRentals = rentalRepository.existsByEquipmentId(id);

        if (hasRentals) {
            throw new BusinessException("No se puede eliminar un equipo con historial de alquileres");
        }

        equipmentRepository.delete(equipment);
    }


    // RF-03
    public Equipment changeStatus(Long id, EquipmentStatus status) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));

        equipment.setStatus(status);

        return equipment;
    }

    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }
}
