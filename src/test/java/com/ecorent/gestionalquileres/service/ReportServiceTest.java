package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.repository.ClientRepository;
import com.ecorent.gestionalquileres.repository.EquipmentRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    EquipmentRepository equipmentRepository;

    @Mock
    ClientRepository clientRepository;

    @Mock
    RentalRepository rentalRepository;

    @InjectMocks
    ReportService reportService;

    // ---------- getTopRentedEquipments ----------

    @Test
    void getTopRentedEquipments_returnsListFromRepository() {
        List<Equipment> equipments = List.of(
                Equipment.builder().id(1L).build(),
                Equipment.builder().id(2L).build()
        );

        when(equipmentRepository.findTopRentedEquipments()).thenReturn(equipments);

        List<Equipment> result = reportService.getTopRentedEquipments();

        assertSame(equipments, result);
        assertEquals(2, result.size());
    }

    // ---------- getIncomeBetween ----------

    @Test
    void getIncomeBetween_returnsValueFromRepository() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(rentalRepository.calculateIncomeBetween(start, end)).thenReturn(1234.56);

        Double result = reportService.getIncomeBetween(start, end);

        assertEquals(1234.56, result);
    }

    // ---------- getTopClients ----------

    @Test
    void getTopClients_returnsListFromRepository() {
        List<Client> clients = List.of(
                Client.builder().id(1L).build(),
                Client.builder().id(2L).build()
        );

        when(clientRepository.findTopClients()).thenReturn(clients);

        List<Client> result = reportService.getTopClients();

        assertSame(clients, result);
        assertEquals(2, result.size());
    }
}
