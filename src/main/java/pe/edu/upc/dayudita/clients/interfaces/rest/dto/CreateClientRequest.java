package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record CreateClientRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String lastName,

        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 20, message = "El documento no puede superar los 20 caracteres")
        String documentNumber,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String phone,

        @NotNull(message = "El dia de corte es obligatorio")
        @Min(value = 1, message = "El dia de corte debe ser mayor o igual a 1")
        @Max(value = 30, message = "El dia de corte debe ser menor o igual a 30")
        Integer cutoffDay,

        @NotNull(message = "El dia de pago es obligatorio")
        @Min(value = 1, message = "El dia de pago debe ser mayor o igual a 1")
        @Max(value = 30, message = "El dia de pago debe ser menor o igual a 30")
        Integer paymentDay

) {
}
