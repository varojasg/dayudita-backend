package pe.edu.upc.dayudita.products.interfaces.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.products.application.ProductService;
import pe.edu.upc.dayudita.products.domain.model.Product;
import pe.edu.upc.dayudita.products.interfaces.rest.dto.ProductResponse;

import java.util.List;

@RestController
@RequestMapping("/api/clients/me/stores/{storeId}/products")
@PreAuthorize("hasRole('CLIENT')")
public class ClientProductController {

    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public ClientProductController(
            ProductService productService,
            CurrentUserService currentUserService
    ){
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ProductResponse> getAvailableProducts(@PathVariable Long storeId){
        Long clientId = currentUserService.getCurrentClientId();

        return productService.getAvailableProductsForClient(clientId, storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product){
        return new ProductResponse(
                product.getId(),
                productCode(product),
                product.getName(),
                product.getSupplier(),
                product.getBrand(),
                product.getDescription(),
                product.getUnitOfMeasure(),
                product.getImageUrl(),
                product.getCashPrice(),
                product.getCreditPrice(),
                product.getAllowsSinglePayment(),
                product.getAllowsInstallments(),
                product.getActive()
        );
    }

    private String productCode(Product product){
        return "DAYU-%04d".formatted(product.getId());
    }
}
