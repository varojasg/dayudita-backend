package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record UpdateClientRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String lastName,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String phone,

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
