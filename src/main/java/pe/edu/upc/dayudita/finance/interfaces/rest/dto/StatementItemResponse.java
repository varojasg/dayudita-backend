package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatementItemResponse(
        Long purchaseId,
        LocalDate purchaseDate,
        BigDecimal principal,
        BigDecimal compensatoryInterest,
        BigDecimal totalAmount
) {
}
