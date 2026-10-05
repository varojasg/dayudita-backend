package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

import java.math.BigDecimal;

public record FinancialConfigurationResponse(
        Long id,
        Long storeId,
        String currency,
        Integer commercialYearDays,
        Integer paymentPeriodDays,
        BigDecimal capitalMinimo,
        BigDecimal capitalMaximo,
        BigDecimal teaMinima,
        BigDecimal teaMaxima,
        Integer plazoMaximoMeses,
        Boolean otorgaGracia
) {
}
