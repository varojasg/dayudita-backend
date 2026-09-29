package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import java.math.BigDecimal;

public record FinancialConfigurationResponse(
        Long id,
        String currency,
        Integer commercialYearDays,
        Integer paymentPeriodDays,
        BigDecimal minAnnualEffectiveRate,
        BigDecimal maxAnnualEffectiveRate,
        BigDecimal annualEffectiveRate,
        BigDecimal moratoryAnnualEffectiveRate,
        BigDecimal minCapital,
        BigDecimal maxCapital,
        BigDecimal creditLimit,
        Integer maxInstallments
) {
}
