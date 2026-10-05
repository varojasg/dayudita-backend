package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long installmentId,
        LocalDate paymentDate,
        BigDecimal teaMoratoriaPactada,
        Integer diasMora,
        BigDecimal amortizacion,
        BigDecimal interes,
        BigDecimal interesMoratorio,
        BigDecimal totalAmount
) {
}
