package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.EquipmentRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    EquipmentRepository equipmentRepository;

    @Mock
    RentalRepository rentalRepository;

    @InjectMocks
    EquipmentService equipmentService;

    // ---------- createEquipment ----------

    @Test
    void createEquipment_whenInternalCodeExists_throwsBusinessException() {
        Equipment equipment = Equipment.builder()
                .internalCode("EQ-001")
                .build();

        when(equipmentRepository.findByInternalCode("EQ-001"))
                .thenReturn(Optional.of(new Equipment()));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.createEquipment(equipment)
        );

        assertEquals("Ya existe un equipo con ese código interno", ex.getMessage());
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void createEquipment_whenInternalCodeNotExists_setsStatusAvailableAndSaves() {
        Equipment equipment = Equipment.builder()
                .name("Taladro")
                .category("Herramientas")
                .internalCode("EQ-001")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.RENTED) // aunque venga así, el servicio lo sobreescribe
                .build();

        when(equipmentRepository.findByInternalCode("EQ-001"))
                .thenReturn(Optional.empty());
        when(equipmentRepository.save(equipment))
                .thenReturn(equipment);

        Equipment result = equipmentService.createEquipment(equipment);

        assertNotNull(result);
        assertEquals(EquipmentStatus.AVAILABLE, result.getStatus());
        verify(equipmentRepository).save(equipment);
    }

    // ---------- updateEquipment ----------

    @Test
    void updateEquipment_whenEquipmentNotFound_throwsBusinessException() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.updateEquipment(1L, new Equipment())
        );

        assertEquals("Equipo no encontrado", ex.getMessage());
    }

    @Test
    void updateEquipment_whenEquipmentIsRented_throwsBusinessException() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .name("Taladro")
                .category("Herramientas")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.RENTED)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.updateEquipment(1L, new Equipment())
        );

        assertEquals("No se puede modificar un equipo alquilado", ex.getMessage());
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void updateEquipment_whenEquipmentAvailable_updatesFields() {
        Equipment existing = Equipment.builder()
                .id(1L)
                .name("Taladro viejo")
                .category("Herramientas viejas")
                .pricePerDay(BigDecimal.ONE)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        Equipment updated = Equipment.builder()
                .name("Taladro nuevo")
                .category("Herramientas")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.MAINTENANCE) // el servicio no toca el status
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        Equipment result = equipmentService.updateEquipment(1L, updated);

        assertSame(existing, result);
        assertEquals("Taladro nuevo", result.getName());
        assertEquals("Herramientas", result.getCategory());
        assertEquals(BigDecimal.TEN, result.getPricePerDay());
        assertEquals(EquipmentStatus.AVAILABLE, result.getStatus(), "El servicio no cambia el status en update");
        verify(equipmentRepository, never()).save(any());
    }

    // ---------- deleteEquipment ----------

    @Test
    void deleteEquipment_whenEquipmentNotFound_throwsBusinessException() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.deleteEquipment(1L)
        );

        assertEquals("Equipo no encontrado", ex.getMessage());
        verify(equipmentRepository, never()).delete(any());
    }

    @Test
    void deleteEquipment_whenEquipmentIsRented_throwsBusinessException() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.RENTED)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.deleteEquipment(1L)
        );

        assertEquals("No se puede eliminar un equipo alquilado", ex.getMessage());
        verify(equipmentRepository, never()).delete(any());
    }

    @Test
    void deleteEquipment_whenEquipmentHasRentalHistory_throwsBusinessException() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository.existsByEquipmentId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.deleteEquipment(1L)
        );

        assertEquals("No se puede eliminar un equipo con historial de alquileres", ex.getMessage());
        verify(equipmentRepository, never()).delete(any());
    }

    @Test
    void deleteEquipment_whenNoRentalsAndNotRented_deletesEquipment() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository.existsByEquipmentId(1L)).thenReturn(false);

        equipmentService.deleteEquipment(1L);

        verify(equipmentRepository).delete(equipment);
    }

    // ---------- changeStatus ----------

    @Test
    void changeStatus_whenEquipmentNotFound_throwsBusinessException() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> equipmentService.changeStatus(1L, EquipmentStatus.MAINTENANCE)
        );

        assertEquals("Equipo no encontrado", ex.getMessage());
    }

    @Test
    void changeStatus_whenEquipmentFound_updatesStatus() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        Equipment result = equipmentService.changeStatus(1L, EquipmentStatus.MAINTENANCE);

        assertSame(equipment, result);
        assertEquals(EquipmentStatus.MAINTENANCE, equipment.getStatus());
        verify(equipmentRepository, never()).save(any());
    }

    // ---------- findAll ----------

    @Test
    void findAll_returnsAllEquipmentsFromRepository() {
        List<Equipment> equipments = List.of(
                Equipment.builder().id(1L).build(),
                Equipment.builder().id(2L).build()
        );

        when(equipmentRepository.findAll()).thenReturn(equipments);

        List<Equipment> result = equipmentService.findAll();

        assertSame(equipments, result);
        assertEquals(2, result.size());
    }
}
