package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    // RF-05
    public Client createClient(Client client) {

        if (clientRepository.existsByDni(client.getDni())) {
            throw new BusinessException("Ya existe un cliente con ese DNI");
        }

        return clientRepository.save(client);
    }

    public Client findByDni(String dni) {
        return clientRepository.findByDni(dni)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado"));
    }
}
