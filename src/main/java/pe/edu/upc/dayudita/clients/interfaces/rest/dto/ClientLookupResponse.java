package pe.edu.upc.dayudita.clients.interfaces.rest.dto;

public record ClientLookupResponse(
        Long id,
        String firstName,
        String lastName,
        String documentNumber,
        String email,
        String phone,
        Boolean alreadyAssociated
) {
}
