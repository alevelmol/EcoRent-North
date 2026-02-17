package com.ecorent.gestionalquileres.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeReportResponse(

        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalIncome
) {}
