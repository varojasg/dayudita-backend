package pe.edu.upc.dayudita.products.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        Boolean active
) {
}
