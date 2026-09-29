package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AssociateClientRequest(

        @NotNull(message = "El dia de corte es obligatorio")
        @Min(value = 1)
        @Max(value = 30)
        Integer cutoffDay,

        @NotNull(message = "El dia de pago es obligatorio")
        @Min(value = 1)
        @Max(value = 30)
        Integer paymentDay

) {
}
