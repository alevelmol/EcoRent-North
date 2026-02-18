package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.entity.Rental;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.ClientRepository;
import com.ecorent.gestionalquileres.repository.EquipmentRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    RentalRepository rentalRepository;

    @Mock
    EquipmentRepository equipmentRepository;

    @Mock
    ClientRepository clientRepository;

    @InjectMocks
    RentalService rentalService;

    // ---------- createRental ----------

    @Test
    void createRental_whenEndBeforeStart_throwsBusinessException() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 9);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.createRental("12345678A", 1L, start, end)
        );

        assertEquals("Fecha fin no puede ser anterior a fecha inicio", ex.getMessage());
        verifyNoInteractions(equipmentRepository, rentalRepository, clientRepository);
    }

    @Test
    void createRental_whenEquipmentNotFound_throwsBusinessException() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 11);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.createRental("12345678A", 1L, start, end)
        );

        assertEquals("Equipo no encontrado", ex.getMessage());
        verify(equipmentRepository).findById(1L);
        verifyNoMoreInteractions(equipmentRepository);
        verifyNoInteractions(rentalRepository, clientRepository);
    }

    @Test
    void createRental_whenEquipmentInMaintenance_throwsBusinessException() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 11);

        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.MAINTENANCE)
                .pricePerDay(BigDecimal.TEN)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.createRental("12345678A", 1L, start, end)
        );

        assertEquals("Equipo en mantenimiento", ex.getMessage());
        verify(equipmentRepository).findById(1L);
        verifyNoInteractions(rentalRepository, clientRepository);
    }

    @Test
    void createRental_whenOverlappingRentalsExist_throwsBusinessException() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 12);

        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .pricePerDay(BigDecimal.TEN)
                .build();

        Rental existingRental = Rental.builder().id(99L).build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository
                .findByEquipmentIdAndReturnedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        1L, end, start))
                .thenReturn(List.of(existingRental));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.createRental("12345678A", 1L, start, end)
        );

        assertEquals("Existe solapamiento de fechas", ex.getMessage());
        verifyNoInteractions(clientRepository);
    }

    @Test
    void createRental_whenClientNotFound_throwsBusinessException() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 12);

        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .pricePerDay(BigDecimal.TEN)
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository
                .findByEquipmentIdAndReturnedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        1L, end, start))
                .thenReturn(List.of());
        when(clientRepository.findByDni("12345678A"))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.createRental("12345678A", 1L, start, end)
        );

        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    void createRental_whenAllValid_createsRentalAndMarksEquipmentAsRented() {
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 12); // 3 días (10, 11, 12)

        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .pricePerDay(BigDecimal.TEN)
                .build();

        Client client = Client.builder()
                .id(5L)
                .dni("12345678A")
                .name("Cliente Prueba")
                .build();

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository
                .findByEquipmentIdAndReturnedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        1L, end, start))
                .thenReturn(List.of());
        when(clientRepository.findByDni("12345678A"))
                .thenReturn(Optional.of(client));
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Rental.class));

        Rental result = rentalService.createRental("12345678A", 1L, start, end);

        assertNotNull(result);
        assertEquals(client, result.getClient());
        assertEquals(equipment, result.getEquipment());
        assertEquals(start, result.getStartDate());
        assertEquals(end, result.getEndDate());
        // 3 días * 10 = 30
        assertEquals(new BigDecimal("30"), result.getTotalAmount());
        assertFalse(result.isReturned());
        assertEquals(EquipmentStatus.RENTED, equipment.getStatus());
        verify(rentalRepository).save(any(Rental.class));
    }

    // ---------- registerReturn ----------

    @Test
    void registerReturn_whenRentalNotFound_throwsBusinessException() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.registerReturn(1L)
        );

        assertEquals("Alquiler no encontrado", ex.getMessage());
    }

    @Test
    void registerReturn_whenAlreadyReturned_throwsBusinessException() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        Rental rental = Rental.builder()
                .id(1L)
                .returned(true)
                .equipment(equipment)
                .build();

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> rentalService.registerReturn(1L)
        );

        assertEquals("El alquiler ya fue devuelto", ex.getMessage());
        assertTrue(rental.isReturned());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
    }

    @Test
    void registerReturn_whenNotReturned_marksAsReturnedAndEquipmentAvailable() {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .status(EquipmentStatus.RENTED)
                .build();

        Rental rental = Rental.builder()
                .id(1L)
                .returned(false)
                .equipment(equipment)
                .build();

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        Rental result = rentalService.registerReturn(1L);

        assertSame(rental, result);
        assertTrue(rental.isReturned());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
        verify(rentalRepository, never()).save(any());
    }

    // ---------- getClientHistory ----------

    @Test
    void getClientHistory_returnsRentalsFromRepository() {
        List<Rental> rentals = List.of(
                Rental.builder().id(1L).build(),
                Rental.builder().id(2L).build()
        );

        when(rentalRepository.findByClientDni("12345678A"))
                .thenReturn(rentals);

        List<Rental> result = rentalService.getClientHistory("12345678A");

        assertSame(rentals, result);
        assertEquals(2, result.size());
    }
}
