package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateFinancialConfigurationRequest(

        @NotNull(message = "La tasa minima es obligatoria")
        @DecimalMin(value = "0.0000001", message = "La tasa minima debe ser mayor a 0")
        BigDecimal minAnnualEffectiveRate,

        @NotNull(message = "La tasa maxima es obligatoria")
        @DecimalMin(value = "0.0000001", message = "La tasa maxima debe ser mayor a 0")
        BigDecimal maxAnnualEffectiveRate,

        @NotNull(message = "La TEA es obligatoria")
        @DecimalMin(value = "0.0000001", message = "La TEA debe ser mayor a 0")
        BigDecimal annualEffectiveRate,

        @NotNull(message = "La tasa moratoria es obligatoria")
        @DecimalMin(value = "0.0000001", message = "La tasa moratoria debe ser mayor a 0")
        BigDecimal moratoryAnnualEffectiveRate,

        @NotNull(message = "El capital minimo es obligatorio")
        @DecimalMin(value = "0.01", message = "El capital minimo debe ser mayor a 0")
        BigDecimal minCapital,

        @NotNull(message = "El capital maximo es obligatorio")
        @DecimalMin(value = "0.01", message = "El capital maximo debe ser mayor a 0")
        BigDecimal maxCapital,

        @NotNull(message = "El limite de credito es obligatorio")
        @DecimalMin(value = "0.01", message = "El limite de credito debe ser mayor a 0")
        BigDecimal creditLimit,

        @NotNull(message = "El maximo de cuotas es obligatorio")
        @Min(value = 1, message = "El maximo de cuotas debe ser mayor a 0")
        Integer maxInstallments

) {
}
