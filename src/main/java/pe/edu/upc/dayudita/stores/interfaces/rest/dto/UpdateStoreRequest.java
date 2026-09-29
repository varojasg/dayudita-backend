package pe.edu.upc.dayudita.stores.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStoreRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 200, message = "La direccion no puede superar los 200 caracteres")
        String address,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String phone
) {
}
