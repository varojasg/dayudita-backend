package pe.edu.upc.dayudita.sales.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.DecimalMin;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseRequest(

        @NotNull(message = "El cliente es obligatorio")
        Long clientId,

        @NotNull(message = "La fecha de compra es obligatoria")
        @PastOrPresent(message = "La fecha de compra no puede ser futura")
        LocalDate purchaseDate,

        @NotNull(message = "La modalidad de pago es obligatoria")
        PurchasePaymentMode paymentMode,

        Integer installmentCount,

        @DecimalMin(value = "0.0000001", message = "La TEA debe ser mayor a 0")
        BigDecimal annualEffectiveRate,

        @NotEmpty(message = "La compra debe tener al menos un producto")
        List<@Valid PurchaseItemRequest> items

) {
}
