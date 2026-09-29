package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import pe.edu.upc.dayudita.finance.domain.model.CreditPlanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreditPlanResponse(
        Long id,
        Long purchaseId,
        Long clientId,
        Long storeId,
        BigDecimal principal,
        BigDecimal capitalizedPrincipal,
        BigDecimal annualEffectiveRate,
        BigDecimal graceRate,
        BigDecimal graceInterest,
        BigDecimal periodRate,
        Integer paymentPeriodDays,
        Integer graceDays,
        Integer installmentCount,
        LocalDate graceEndDate,
        CreditPlanStatus status,
        List<InstallmentResponse> installments
) {
}
