package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterPaymentRequest(
        @NotNull(message = "La fecha de pago es obligatoria")
        LocalDate paymentDate
) {
}
