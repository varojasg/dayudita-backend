package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import pe.edu.upc.dayudita.finance.domain.model.StatementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountStatementResponse(
        Long id,
        Long clientId,
        Long storeId,
        LocalDate cutoffDate,
        LocalDate dueDate,
        BigDecimal annualEffectiveRate,
        BigDecimal principal,
        BigDecimal compensatoryInterest,
        BigDecimal totalAmount,
        StatementStatus status,
        LocalDate paymentDate,
        Integer overdueDays,
        BigDecimal moratoryInterest,
        BigDecimal amountWithMoratory,
        List<StatementItemResponse> items
) {
}
