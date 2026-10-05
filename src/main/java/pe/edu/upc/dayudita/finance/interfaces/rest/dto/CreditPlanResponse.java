package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import pe.edu.upc.dayudita.finance.domain.model.CreditPlanStatus;

import java.math.BigDecimal;
import java.util.List;

public record CreditPlanResponse(
        Long id,
        Long purchaseId,
        Long clientId,
        Long storeId,
        BigDecimal ventaPrecio,
        BigDecimal porcentajeCuotaInicial,
        BigDecimal cuotaInicial,
        BigDecimal principal,
        BigDecimal teaPactada,
        BigDecimal teq,
        BigDecimal teaMoratoriaPactada,
        Integer paymentPeriodDays,
        Integer numeroMeses,
        Integer installmentCount,
        Integer diasGraciaTotal,
        BigDecimal totalIntereses,
        BigDecimal interesCapitalizado,
        BigDecimal totalAmortizacion,
        BigDecimal totalCuotas,
        BigDecimal totalAPagar,
        BigDecimal totalInteresMoratorio,
        BigDecimal totalAPagarConMora,
        CreditPlanStatus status,
        List<InstallmentResponse> installments
) {
}
