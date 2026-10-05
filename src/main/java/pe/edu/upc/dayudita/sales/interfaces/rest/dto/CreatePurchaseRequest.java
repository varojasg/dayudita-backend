package pe.edu.upc.dayudita.sales.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseRequest(

        @NotNull(message = "El cliente es obligatorio")
        Long clientId,

        @NotNull(message = "La fecha de compra es obligatoria")
        LocalDate purchaseDate,

        @NotNull(message = "La modalidad de pago es obligatoria")
        PurchasePaymentMode paymentMode,

        @DecimalMin(value = "0.0", message = "El porcentaje de cuota inicial no puede ser negativo")
        @DecimalMax(value = "0.5", message = "El porcentaje de cuota inicial no puede superar el 50%")
        BigDecimal porcentajeCuotaInicial,

        Integer numeroMeses,

        @NotEmpty(message = "La compra debe tener al menos un producto")
        List<@Valid PurchaseItemRequest> items

) {
}
