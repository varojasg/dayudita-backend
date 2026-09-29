package pe.edu.upc.dayudita.products.interfaces.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 100, message = "El proveedor no puede superar los 100 caracteres")
        String supplier,

        @Size(max = 100, message = "La marca no puede superar los 100 caracteres")
        String brand,

        @Size(max = 300, message = "La descripcion no puede superar los 300 caracteres")
        String description,

        @Size(max = 50, message = "La unidad de medida no puede superar los 50 caracteres")
        String unitOfMeasure,

        @Size(max = 500, message = "La URL de imagen no puede superar los 500 caracteres")
        String imageUrl,

        @NotNull(message = "El precio al contado es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio al contado debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio al contado debe tener maximo 2 decimales")
        BigDecimal cashPrice,

        @NotNull(message = "El precio a credito es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio a credito debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio a credito debe tener maximo 2 decimales")
        BigDecimal creditPrice,

        @NotNull(message = "Debe indicar si permite pago unico")
        Boolean allowsSinglePayment,

        @NotNull(message = "Debe indicar si permite cuotas")
        Boolean allowsInstallments

) {
}
