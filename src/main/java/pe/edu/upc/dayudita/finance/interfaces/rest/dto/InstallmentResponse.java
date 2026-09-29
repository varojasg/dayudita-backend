package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import pe.edu.upc.dayudita.finance.domain.model.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentResponse(
        Long id,
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal openingBalance,
        BigDecimal interest,
        BigDecimal amortization,
        BigDecimal amount,
        BigDecimal remainingBalance,
        InstallmentStatus status,
        LocalDate paymentDate,
        Integer overdueDays,
        BigDecimal moratoryInterest,
        BigDecimal amountWithMoratory
) {
}
