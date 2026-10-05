package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import pe.edu.upc.dayudita.finance.domain.model.GraceType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentResponse(
        Long id,
        Integer installmentNumber,
        GraceType type,
        LocalDate dueDate,
        BigDecimal tasaPeriodo,
        Integer diasPeriodo,
        BigDecimal openingBalance,
        BigDecimal interest,
        BigDecimal amount,
        BigDecimal amortization,
        BigDecimal remainingBalance,
        Integer diasMora,
        BigDecimal tasaMoratoriaTramo,
        BigDecimal interesMoratorio,
        BigDecimal pagoTotal,
        InstallmentDisplayStatus estado,
        LocalDate paymentDate
) {
}
