package pe.edu.upc.dayudita.iam.interfaces.rest.dto;

public record AuthResponse(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String role,
        Long storeId,
        String storeName
) {
}
