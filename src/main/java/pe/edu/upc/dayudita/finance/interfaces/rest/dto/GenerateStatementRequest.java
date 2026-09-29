package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record GenerateStatementRequest(
        @NotNull(message = "La fecha de corte es obligatoria")
        @PastOrPresent(message = "La fecha de corte no puede ser futura")
        LocalDate cutoffDate
) {
}
