package com.ecorent.gestionalquileres.controller;

import com.ecorent.gestionalquileres.entity.Client;
import com.ecorent.gestionalquileres.entity.Equipment;
import com.ecorent.gestionalquileres.entity.EquipmentStatus;
import com.ecorent.gestionalquileres.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ReportService reportService;

    // ---------- GET /api/reports/income ----------

    @Test
    void income_returnsIncomeReportResponse_whenValidRequest() throws Exception {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        given(reportService.getIncomeBetween(start, end))
                .willReturn(1234.56);

        mockMvc.perform(get("/api/reports/income")
                        .param("start", "2024-01-01")
                        .param("end", "2024-01-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2024-01-01"))
                .andExpect(jsonPath("$.endDate").value("2024-01-31"))
                .andExpect(jsonPath("$.totalIncome").value(1234.56));
    }

    @Test
    void income_returnsInternalServerError_whenUnexpectedException() throws Exception {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        given(reportService.getIncomeBetween(start, end))
                .willThrow(new RuntimeException("Error inesperado en reporte"));

        mockMvc.perform(get("/api/reports/income")
                        .param("start", "2024-01-01")
                        .param("end", "2024-01-31"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error inesperado"));
    }

    // ---------- GET /api/reports/top-equipments ----------

    @Test
    void topEquipments_returnsListOfEquipments() throws Exception {
        Equipment e1 = Equipment.builder()
                .id(1L)
                .name("Taladro")
                .category("Herramientas")
                .internalCode("EQ-001")
                .pricePerDay(BigDecimal.TEN)
                .status(EquipmentStatus.RENTED)
                .build();

        Equipment e2 = Equipment.builder()
                .id(2L)
                .name("Generador")
                .category("Energía")
                .internalCode("EQ-002")
                .pricePerDay(BigDecimal.ONE)
                .status(EquipmentStatus.AVAILABLE)
                .build();

        given(reportService.getTopRentedEquipments())
                .willReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/reports/top-equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Taladro"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Generador"));
    }

    // ---------- GET /api/reports/top-clients ----------

    @Test
    void topClients_returnsListOfClients() throws Exception {
        Client c1 = Client.builder()
                .id(1L)
                .name("Cliente 1")
                .dni("111A")
                .build();
        Client c2 = Client.builder()
                .id(2L)
                .name("Cliente 2")
                .dni("222B")
                .build();

        given(reportService.getTopClients())
                .willReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/reports/top-clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Cliente 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Cliente 2"));
    }
}
