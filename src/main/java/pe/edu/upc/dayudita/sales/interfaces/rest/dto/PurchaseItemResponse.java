package pe.edu.upc.dayudita.sales.interfaces.rest.dto;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
