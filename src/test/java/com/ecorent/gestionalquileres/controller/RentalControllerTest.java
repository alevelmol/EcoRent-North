package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.rental.RentalRequest;
import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.entity.Rental;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RentalService rentalService;

    // ---------- POST /api/rentals ----------

    @Test
    void createRental_returnsRentalResponse_whenValidRequest() throws Exception {
        RentalRequest request = new RentalRequest(
                "12345678A",
                10L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 3)
        );

        Client client = Client.builder()
                .id(1L)
                .name("Juan")
                .dni("12345678A")
                .build();

        Equipment equipment = Equipment.builder()
                .id(10L)
                .name("Taladro")
                .status(EquipmentStatus.AVAILABLE)
                .pricePerDay(BigDecimal.TEN)
                .build();

        Rental rental = Rental.builder()
                .id(100L)
                .client(client)
                .equipment(equipment)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalAmount(new BigDecimal("30"))
                .returned(false)
                .build();

        given(rentalService.createRental(
                Mockito.eq("12345678A"),
                Mockito.eq(10L),
                Mockito.eq(request.startDate()),
                Mockito.eq(request.endDate())
        )).willReturn(rental);

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.clientName").value("Juan"))
                .andExpect(jsonPath("$.clientDni").value("12345678A"))
                .andExpect(jsonPath("$.equipmentId").value(10L))
                .andExpect(jsonPath("$.equipmentName").value("Taladro"))
                .andExpect(jsonPath("$.totalAmount").value(30))
                .andExpect(jsonPath("$.returned").value(false));
    }

    @Test
    void createRental_returnsBadRequest_whenBusinessException() throws Exception {
        RentalRequest request = new RentalRequest(
                "12345678A",
                10L,
                LocalDate.of(2024, 1, 10),
                LocalDate.of(2024, 1, 9) // o cualquier caso que provoque BusinessException
        );

        given(rentalService.createRental(
                Mockito.eq("12345678A"),
                Mockito.eq(10L),
                Mockito.eq(request.startDate()),
                Mockito.eq(request.endDate())
        )).willThrow(new BusinessException("Fecha fin no puede ser anterior a fecha inicio"));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("Fecha fin no puede ser anterior a fecha inicio"));
    }

    @Test
    void createRental_returnsNotFound_whenClientOrEquipmentNotFound() throws Exception {
        RentalRequest request = new RentalRequest(
                "99999999X",
                99L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 3)
        );

        given(rentalService.createRental(
                Mockito.eq("99999999X"),
                Mockito.eq(99L),
                Mockito.eq(request.startDate()),
                Mockito.eq(request.endDate())
        )).willThrow(new NotFoundException("Cliente no encontrado"));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void createRental_returnsBadRequest_whenValidationFails() throws Exception {
        String invalidJson = """
                {
                  "clientDni": "",
                  "equipmentId": null,
                  "startDate": null,
                  "endDate": null
                }
                """;

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ---------- PUT /api/rentals/{id}/return ----------

    @Test
    void registerReturn_returnsRentalReturnResponse_whenSuccess() throws Exception {
        Client client = Client.builder()
                .id(1L)
                .name("Juan")
                .dni("12345678A")
                .build();

        Equipment equipment = Equipment.builder()
                .id(10L)
                .name("Taladro")
                .status(EquipmentStatus.AVAILABLE)
                .build();

        Rental rental = Rental.builder()
                .id(100L)
                .client(client)
                .equipment(equipment)
                .returned(true)
                .build();

        given(rentalService.registerReturn(100L)).willReturn(rental);

        mockMvc.perform(put("/api/rentals/100/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalId").value(100L))
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.equipmentStatus").value("AVAILABLE"));
    }

    @Test
    void registerReturn_returnsNotFound_whenRentalDoesNotExist() throws Exception {
        given(rentalService.registerReturn(999L))
                .willThrow(new NotFoundException("Alquiler no encontrado"));

        mockMvc.perform(put("/api/rentals/999/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Alquiler no encontrado"));
    }

    @Test
    void registerReturn_returnsBadRequest_whenBusinessException() throws Exception {
        given(rentalService.registerReturn(100L))
                .willThrow(new BusinessException("El alquiler ya fue devuelto"));

        mockMvc.perform(put("/api/rentals/100/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("El alquiler ya fue devuelto"));
    }

    // ---------- GET /api/rentals/{dni}/rentals ----------

    @Test
    void getClientHistory_returnsRentalsList() throws Exception {
        Client client = Client.builder()
                .id(1L)
                .name("Juan")
                .dni("12345678A")
                .build();

        Equipment equipment = Equipment.builder()
                .id(10L)
                .name("Taladro")
                .status(EquipmentStatus.AVAILABLE)
                .build();

        Rental rental1 = Rental.builder()
                .id(100L)
                .client(client)
                .equipment(equipment)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 3))
                .totalAmount(new BigDecimal("30"))
                .returned(false)
                .build();

        Rental rental2 = Rental.builder()
                .id(101L)
                .client(client)
                .equipment(equipment)
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 2))
                .totalAmount(new BigDecimal("20"))
                .returned(true)
                .build();

        given(rentalService.getClientHistory("12345678A"))
                .willReturn(List.of(rental1, rental2));

        mockMvc.perform(get("/api/rentals/12345678A/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[1].id").value(101L));
    }
}
