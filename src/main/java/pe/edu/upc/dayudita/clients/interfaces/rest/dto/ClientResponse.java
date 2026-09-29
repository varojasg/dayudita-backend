package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

public record ClientResponse(

        Long id,
        Long clientAccountId,
        String firstName,
        String lastName,
        String documentNumber,
        String email,
        String phone,
        Integer cutoffDay,
        Integer paymentDay,
        Boolean active

) {
}
