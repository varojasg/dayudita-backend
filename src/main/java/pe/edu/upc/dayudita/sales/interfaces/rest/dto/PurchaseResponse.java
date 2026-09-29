package pe.edu.upc.dayudita.sales.interfaces.rest.dto;

import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseResponse(
        Long id,
        Long clientId,
        Long storeId,
        LocalDate purchaseDate,
        PurchasePaymentMode paymentMode,
        PurchaseStatus status,
        BigDecimal total,
        BigDecimal annualEffectiveRate,
        Integer installmentCount,
        List<PurchaseItemResponse> items
) {
}
