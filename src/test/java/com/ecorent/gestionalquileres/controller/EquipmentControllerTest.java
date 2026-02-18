package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.dto.equipment.EquipmentRequest;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.exception.BusinessException;
import com.ecorent.gestionalquileres.exception.NotFoundException;
import com.ecorent.gestionalquileres.service.EquipmentService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EquipmentService equipmentService;

    // ---------- POST /api/equipments ----------

    @Test
    void createEquipment_returnsEquipmentResponse_whenValidRequest() throws Exception {
        EquipmentRequest request = new EquipmentRequest(
                "Taladro",
                "Herramientas",
                "EQ-001",
                BigDecimal.TEN
        );

        Equipment saved = Equipment.builder()
                .id(1L)
                .name("Taladro")
                .category("Herramientas")
                .internalCode("EQ-001")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        given(equipmentService.createEquipment(any(Equipment.class))).willReturn(saved);

        mockMvc.perform(post("/api/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Taladro"))
                .andExpect(jsonPath("$.category").value("Herramientas"))
                .andExpect(jsonPath("$.internalCode").value("EQ-001"))
                .andExpect(jsonPath("$.pricePerDay").value(10))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createEquipment_returnsBadRequest_whenBusinessException() throws Exception {
        EquipmentRequest request = new EquipmentRequest(
                "Taladro",
                "Herramientas",
                "EQ-001",
                BigDecimal.TEN
        );

        given(equipmentService.createEquipment(any(Equipment.class)))
                .willThrow(new BusinessException("Ya existe un equipo con ese código interno"));

        mockMvc.perform(post("/api/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("Ya existe un equipo con ese código interno"));
    }

    @Test
    void createEquipment_returnsBadRequest_whenValidationFails() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "category": "",
                  "internalCode": "",
                  "pricePerDay": 0
                }
                """;

        mockMvc.perform(post("/api/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ---------- GET /api/equipments ----------

    @Test
    void findAll_returnsListOfEquipments() throws Exception {
        Equipment e1 = Equipment.builder()
                .id(1L)
                .name("Taladro")
                .category("Herramientas")
                .internalCode("EQ-001")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        Equipment e2 = Equipment.builder()
                .id(2L)
                .name("Generador")
                .category("Energía")
                .internalCode("EQ-002")
                .pricePerDay(BigDecimal.ONE)
                .status(EquipmentStatus.RENTED)
                .build();

        given(equipmentService.findAll()).willReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    // ---------- PUT /api/equipments/{id} ----------

    @Test
    void updateEquipment_returnsUpdatedEquipment_whenExists() throws Exception {
        Equipment updated = Equipment.builder()
                .id(1L)
                .name("Taladro Pro")
                .category("Herramientas")
                .internalCode("EQ-001")
                .pricePerDay(BigDecimal.valueOf(20))
                .status(EquipmentStatus.AVAILABLE)
                .build();

        given(equipmentService.updateEquipment(Mockito.eq(1L), any(Equipment.class)))
                .willReturn(updated);

        String body = """
                {
                  "name": "Taladro Pro",
                  "category": "Herramientas",
                  "internalCode": "IGNORADO",
                  "pricePerDay": 20,
                  "status": "MAINTENANCE"
                }
                """;

        mockMvc.perform(put("/api/equipments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Taladro Pro"))
                .andExpect(jsonPath("$.pricePerDay").value(20));
    }

    @Test
    void updateEquipment_returnsNotFound_whenEquipmentDoesNotExist() throws Exception {
        given(equipmentService.updateEquipment(Mockito.eq(99L), any(Equipment.class)))
                .willThrow(new NotFoundException("Equipo no encontrado"));

        String body = """
                {
                  "name": "Taladro",
                  "category": "Herramientas",
                  "internalCode": "EQ-001",
                  "pricePerDay": 10
                }
                """;

        mockMvc.perform(put("/api/equipments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Equipo no encontrado"));
    }

    @Test
    void updateEquipment_returnsBadRequest_whenBusinessException() throws Exception {
        given(equipmentService.updateEquipment(Mockito.eq(1L), any(Equipment.class)))
                .willThrow(new BusinessException("No se puede modificar un equipo alquilado"));

        String body = """
                {
                  "name": "Taladro",
                  "category": "Herramientas",
                  "internalCode": "EQ-001",
                  "pricePerDay": 10
                }
                """;

        mockMvc.perform(put("/api/equipments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("No se puede modificar un equipo alquilado"));
    }

    // ---------- DELETE /api/equipments/{id} ----------

    @Test
    void deleteEquipment_returnsNoContent_whenSuccess() throws Exception {
        mockMvc.perform(delete("/api/equipments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEquipment_returnsNotFound_whenEquipmentDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Equipo no encontrado"))
                .when(equipmentService).deleteEquipment(99L);

        mockMvc.perform(delete("/api/equipments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Equipo no encontrado"));
    }

    @Test
    void deleteEquipment_returnsBadRequest_whenHasRentalHistoryOrRented() throws Exception {
        doThrow(new BusinessException("No se puede eliminar un equipo con historial de alquileres"))
                .when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(delete("/api/equipments/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Error"))
                .andExpect(jsonPath("$.message").value("No se puede eliminar un equipo con historial de alquileres"));
    }

    // ---------- PUT /api/equipments/{id}/status ----------

    @Test
    void updateStatus_returnsUpdatedEquipment_whenExists() throws Exception {
        Equipment equipment = Equipment.builder()
                .id(1L)
                .name("Taladro")
                .category("Herramientas")
                .internalCode("EQ-001")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.MAINTENANCE)
                .build();

        given(equipmentService.changeStatus(1L, EquipmentStatus.MAINTENANCE))
                .willReturn(equipment);

        String body = """
                {
                  "status": "MAINTENANCE"
                }
                """;

        mockMvc.perform(put("/api/equipments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    void updateStatus_returnsNotFound_whenEquipmentDoesNotExist() throws Exception {
        given(equipmentService.changeStatus(99L, EquipmentStatus.MAINTENANCE))
                .willThrow(new NotFoundException("Equipo no encontrado"));

        String body = """
                {
                  "status": "MAINTENANCE"
                }
                """;

        mockMvc.perform(put("/api/equipments/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Equipo no encontrado"));
    }

    @Test
    void updateStatus_returnsBadRequest_whenValidationFails() throws Exception {
        // Falta el campo status
        String invalidJson = "{}";

        mockMvc.perform(put("/api/equipments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }
}
