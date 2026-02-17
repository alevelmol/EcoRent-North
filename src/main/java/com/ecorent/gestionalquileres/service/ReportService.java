package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EquipmentRepository equipmentRepository;
    private final ClientRepository clientRepository;
    private final RentalRepository rentalRepository;

    public List<Equipment> getTopRentedEquipments() {
        return equipmentRepository.findTopRentedEquipments();
    }

    public Double getIncomeBetween(LocalDate start, LocalDate end) {
        return rentalRepository.calculateIncomeBetween(start, end);
    }

    public List<Client> getTopClients() {
        return clientRepository.findTopClients();
    }
}
