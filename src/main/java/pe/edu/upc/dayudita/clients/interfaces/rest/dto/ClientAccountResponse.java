package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import pe.edu.upc.dayudita.clients.domain.model.CreditCurrency;

import java.math.BigDecimal;

public record ClientAccountResponse(
        Long clientAccountId,
        Long storeId,
        String storeName,
        BigDecimal teaPactada,
        BigDecimal teaMoratoriaPactada,
        CreditCurrency currency,
        BigDecimal limiteCredito,
        Integer plazoMaximoMeses,
        Integer diaCorte,
        Boolean active
) {
}
