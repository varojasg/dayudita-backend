package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record RegisterPaymentRequest(
        @NotNull(message = "La fecha de pago es obligatoria")
        @PastOrPresent(message = "La fecha de pago no puede ser futura")
        LocalDate paymentDate
) {
}
