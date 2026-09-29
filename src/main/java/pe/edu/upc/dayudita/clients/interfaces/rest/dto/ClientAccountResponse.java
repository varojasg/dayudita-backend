package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

public record ClientAccountResponse(
        Long clientAccountId,
        Long storeId,
        String storeName,
        Integer cutoffDay,
        Integer paymentDay,
        Boolean active
) {
}
