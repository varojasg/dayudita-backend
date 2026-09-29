package pe.edu.upc.dayudita.stores.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStoreStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        Boolean active
) {
}
