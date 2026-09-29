package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateClientStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        Boolean active
) {
}
