package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.client.ClientRequest;
import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.entity.Rental;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.service.ClientService;
import com.ecorent.gestionalquileres.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ClientService clientService;

    @MockBean
    RentalService rentalService;

    // ---------- POST /api/clients ----------

    @Test
    void createClient_returnsClientResponse_whenValidRequest() throws Exception {
        ClientRequest request = new ClientRequest(
                "Juan",
                "12345678A",
                "600000000",
                "juan@example.com"
        );

        Client saved = Client.builder()
                .id(1L)
                .name("Juan")
                .dni("12345678A")
                .phone("600000000")
                .email("juan@example.com")
                .build();

        given(clientService.createClient(any(Client.class))).willReturn(saved);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.dni").value("12345678A"))
                .andExpect(jsonPath("$.phone").value("600000000"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));
    }

    @Test
    void createClient_returnsBadRequest_whenBusinessException() throws Exception {
        ClientRequest request = new ClientRequest(
                "Juan",
                "12345678A",
                "600000000",
                "juan@example.com"
        );

        given(clientService.createClient(any(Client.class)))
                .willThrow(new BusinessException("Ya existe un cliente con ese DNI"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("Ya existe un cliente con ese DNI"));
    }

    @Test
    void createClient_returnsBadRequest_whenValidationFails() throws Exception {
        // name y dni en blanco, email inválido
        String invalidJson = """
                {
                  "name": "",
                  "dni": "",
                  "phone": "",
                  "email": "no-es-email"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ---------- PUT /api/clients/{id} ----------

    @Test
    void updateClient_returnsUpdatedClient_whenExists() throws Exception {
        Client updated = Client.builder()
                .id(1L)
                .name("Nuevo Nombre")
                .dni("12345678A")
                .phone("700000000")
                .email("nuevo@example.com")
                .build();

        given(clientService.updateClient(Mockito.eq(1L), any(Client.class)))
                .willReturn(updated);

        String body = """
                {
                  "name": "Nuevo Nombre",
                  "dni": "IGNORADO",
                  "phone": "700000000",
                  "email": "nuevo@example.com"
                }
                """;

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.dni").value("12345678A"))
                .andExpect(jsonPath("$.phone").value("700000000"))
                .andExpect(jsonPath("$.email").value("nuevo@example.com"));
    }

    @Test
    void updateClient_returnsNotFound_whenClientDoesNotExist() throws Exception {
        given(clientService.updateClient(Mockito.eq(99L), any(Client.class)))
                .willThrow(new NotFoundException("Cliente no encontrado"));

        String body = """
                {
                  "name": "Nombre",
                  "dni": "12345678A",
                  "phone": "600000000",
                  "email": "mail@example.com"
                }
                """;

        mockMvc.perform(put("/api/clients/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    // ---------- DELETE /api/clients/{id} ----------

    @Test
    void deleteClient_returnsNoContent_whenSuccess() throws Exception {
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteClient_returnsNotFound_whenClientDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Cliente no encontrado"))
                .when(clientService).deleteClient(99L);

        mockMvc.perform(delete("/api/clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void deleteClient_returnsBadRequest_whenHasRentalHistory() throws Exception {
        doThrow(new BusinessException("No se puede eliminar un cliente con historial de alquileres"))
                .when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("No se puede eliminar un cliente con historial de alquileres"));
    }

    // ---------- GET /api/clients/{dni}/rentals ----------

    @Test
    void getHistory_returnsRentalResponses() throws Exception {
        Client client = Client.builder()
                .id(1L)
                .name("Juan")
                .dni("12345678A")
                .build();

        Equipment equipment = Equipment.builder()
                .id(10L)
                .name("Taladro")
                .build();

        Rental rental = Rental.builder()
                .id(100L)
                .client(client)
                .equipment(equipment)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 3))
                .totalAmount(new BigDecimal("30"))
                .returned(false)
                .build();

        given(rentalService.getClientHistory("12345678A"))
                .willReturn(List.of(rental));

        mockMvc.perform(get("/api/clients/12345678A/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].clientName").value("Juan"))
                .andExpect(jsonPath("$[0].clientDni").value("12345678A"))
                .andExpect(jsonPath("$[0].equipmentId").value(10L))
                .andExpect(jsonPath("$[0].equipmentName").value("Taladro"));
    }

    // ---------- GET /api/clients ----------

    @Test
    void findAll_returnsListOfClients() throws Exception {
        Client c1 = Client.builder().id(1L).name("A").dni("1").build();
        Client c2 = Client.builder().id(2L).name("B").dni("2").build();

        given(clientService.findAll()).willReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }
}
