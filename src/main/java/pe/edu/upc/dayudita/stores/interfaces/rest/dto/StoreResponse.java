package pe.edu.upc.dayudita.stores.interfaces.rest.dto;

public record StoreResponse(
        Long id,
        String name,
        String address,
        String phone,
        Boolean active
) {
}
