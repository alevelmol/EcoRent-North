package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    // RF-01
    public Equipment createEquipment(Equipment equipment) {

        if (equipmentRepository.findByInternalCode(equipment.getInternalCode()).isPresent()) {
            throw new BusinessException("Ya existe un equipo con ese código interno");
        }

        equipment.setStatus(EquipmentStatus.AVAILABLE);

        return equipmentRepository.save(equipment);
    }

    // RF-02
    public Equipment updateEquipment(Long id, Equipment updated) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Equipo no encontrado"));

        equipment.setName(updated.getName());
        equipment.setCategory(updated.getCategory());
        equipment.setPricePerDay(updated.getPricePerDay());

        return equipment;
    }

    // RF-03
    public Equipment changeStatus(Long id, EquipmentStatus status) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Equipo no encontrado"));

        equipment.setStatus(status);

        return equipment;
    }

    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }
}