package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import jakarta.validation.constraints.*;
import pe.edu.upc.dayudita.clients.domain.model.CreditCurrency;

import java.math.BigDecimal;

public record AssociateClientRequest(

        @NotNull(message = "La TEA pactada es obligatoria")
        @DecimalMin(value = "0.0000001", message = "La TEA pactada debe ser mayor a 0")
        BigDecimal teaPactada,

        @NotNull(message = "La moneda del credito es obligatoria")
        CreditCurrency currency,

        @NotNull(message = "El limite de credito es obligatorio")
        @DecimalMin(value = "0.01", message = "El limite de credito debe ser mayor a 0")
        BigDecimal limiteCredito,

        @NotNull(message = "El plazo maximo en meses es obligatorio")
        @Min(value = 1, message = "El plazo maximo debe ser al menos 1 mes")
        Integer plazoMaximoMeses,

        @NotNull(message = "El dia de corte es obligatorio")
        @Min(value = 1, message = "El dia de corte debe estar entre 1 y 31")
        @Max(value = 31, message = "El dia de corte debe estar entre 1 y 31")
        Integer diaCorte

) {
}
