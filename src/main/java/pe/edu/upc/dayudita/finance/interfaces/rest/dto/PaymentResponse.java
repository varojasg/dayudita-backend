package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import pe.edu.upc.dayudita.finance.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        PaymentType type,
        Long statementId,
        Long installmentId,
        LocalDate paymentDate,
        BigDecimal moratoryAnnualEffectiveRate,
        BigDecimal principalAmount,
        BigDecimal compensatoryInterest,
        BigDecimal moratoryInterest,
        BigDecimal totalAmount
) {
}
