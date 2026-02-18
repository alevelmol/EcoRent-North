package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.repository.ClientRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final RentalRepository rentalRepository;

    // RF-05
    public Client createClient(Client client) {

        if (clientRepository.existsByDni(client.getDni())) {
            throw new BusinessException("Ya existe un cliente con ese DNI");
        }

        return clientRepository.save(client);
    }
    
    public Client updateClient(Long id, Client updated) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        client.setName(updated.getName());
        client.setPhone(updated.getPhone());
        client.setEmail(updated.getEmail());

        return client;
    }

    public void deleteClient(Long id) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        boolean hasRentals = rentalRepository.existsByClientId(id);

        if (hasRentals) {
            throw new BusinessException("No se puede eliminar un cliente con historial de alquileres");
        }

        clientRepository.delete(client);
    }


    public Client findByDni(String dni) {
        return clientRepository.findByDni(dni)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }
    
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    
}
