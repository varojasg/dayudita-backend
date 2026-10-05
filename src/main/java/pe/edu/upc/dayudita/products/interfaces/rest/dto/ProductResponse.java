package pe.edu.upc.dayudita.products.interfaces.rest.dto;

import java.math.BigDecimal;

public record ProductResponse(

        Long id,
        String code,
        String name,
        String supplier,
        String brand,
        String description,
        String unitOfMeasure,
        String imageUrl,
        BigDecimal cashPrice,
        BigDecimal creditPrice,
        Boolean allowsInstallments,
        Boolean active

) {
}
