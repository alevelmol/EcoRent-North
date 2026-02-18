package com.ecorent.gestionalquileres.service;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.repository.ClientRepository;
import com.ecorent.gestionalquileres.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    ClientRepository clientRepository;

    @Mock
    RentalRepository rentalRepository;

    @InjectMocks
    ClientService clientService;

    @Test
    void createClient_whenDniAlreadyExists_throwsBusinessException() {
        Client client = Client.builder()
                .dni("12345678A")
                .build();

        when(clientRepository.existsByDni("12345678A")).thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> clientService.createClient(client)
        );

        assertEquals("Ya existe un cliente con ese DNI", ex.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void createClient_whenDniNotExists_savesClient() {
        Client client = Client.builder()
                .dni("12345678A")
                .name("John Doe")
                .build();

        Client saved = Client.builder()
                .id(1L)
                .dni("12345678A")
                .name("John Doe")
                .build();

        when(clientRepository.existsByDni("12345678A")).thenReturn(false);
        when(clientRepository.save(client)).thenReturn(saved);

        Client result = clientService.createClient(client);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345678A", result.getDni());
        verify(clientRepository).save(client);
    }

    @Test
    void updateClient_whenClientNotFound_throwsBusinessException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> clientService.updateClient(1L, new Client())
        );

        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    void updateClient_whenClientFound_updatesFields() {
        Client existing = Client.builder()
                .id(1L)
                .dni("12345678A")
                .name("Old Name")
                .phone("111111111")
                .email("old@example.com")
                .build();

        Client updated = Client.builder()
                .name("New Name")
                .phone("222222222")
                .email("new@example.com")
                .dni("OTHER") // no debería cambiarse
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));

        Client result = clientService.updateClient(1L, updated);

        assertSame(existing, result);
        assertEquals("New Name", result.getName());
        assertEquals("222222222", result.getPhone());
        assertEquals("new@example.com", result.getEmail());
        // DNI no se modifica en el servicio
        assertEquals("12345678A", result.getDni());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClient_whenClientNotFound_throwsBusinessException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> clientService.deleteClient(1L)
        );

        assertEquals("Cliente no encontrado", ex.getMessage());
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void deleteClient_whenClientHasRentals_throwsBusinessException() {
        Client client = Client.builder().id(1L).build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(rentalRepository.existsByClientId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> clientService.deleteClient(1L)
        );

        assertEquals("No se puede eliminar un cliente con historial de alquileres", ex.getMessage());
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void deleteClient_whenClientHasNoRentals_deletesClient() {
        Client client = Client.builder().id(1L).build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(rentalRepository.existsByClientId(1L)).thenReturn(false);

        clientService.deleteClient(1L);

        verify(clientRepository).delete(client);
    }

    @Test
    void findByDni_whenClientNotFound_throwsBusinessException() {
        when(clientRepository.findByDni("12345678A")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> clientService.findByDni("12345678A")
        );

        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    void findByDni_whenClientFound_returnsClient() {
        Client client = Client.builder()
                .id(1L)
                .dni("12345678A")
                .build();

        when(clientRepository.findByDni("12345678A")).thenReturn(Optional.of(client));

        Client result = clientService.findByDni("12345678A");

        assertSame(client, result);
    }

    @Test
    void findAll_returnsAllClientsFromRepository() {
        List<Client> clients = List.of(
                Client.builder().id(1L).build(),
                Client.builder().id(2L).build()
        );

        when(clientRepository.findAll()).thenReturn(clients);

        List<Client> result = clientService.findAll();

        assertSame(clients, result);
        assertEquals(2, result.size());
    }
}
