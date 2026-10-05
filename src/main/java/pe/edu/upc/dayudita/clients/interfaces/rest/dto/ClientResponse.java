package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

import pe.edu.upc.dayudita.clients.domain.model.CreditCurrency;

import java.math.BigDecimal;

public record ClientResponse(

        Long id,
        Long clientAccountId,
        String firstName,
        String lastName,
        String documentNumber,
        String email,
        String phone,
        BigDecimal teaPactada,
        BigDecimal teaMoratoriaPactada,
        CreditCurrency currency,
        BigDecimal limiteCredito,
        Integer plazoMaximoMeses,
        Integer diaCorte,
        Boolean active

) {
}
