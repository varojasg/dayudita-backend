package pe.edu.upc.dayudita.products.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.products.application.ProductService;
import pe.edu.upc.dayudita.products.domain.model.Product;
import pe.edu.upc.dayudita.products.interfaces.rest.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/products")
@PreAuthorize("hasRole('STORE_ADMIN')")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getProductsByStore(@PathVariable Long storeId){
        return productService.getProductsByStore(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateProductRequest request
    ){
        return toResponse(productService.createProduct(storeId, request));
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request
    ){
        return toResponse(productService.updateProduct(storeId, productId, request));
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse updateProductStatus(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductStatusRequest request
    ){
        return toResponse(productService.updateProductStatus(storeId, productId, request.active()));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId
    ){
        productService.deactivateProduct(storeId, productId);
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
                product.getAllowsInstallments(),
                product.getActive()
        );
    }

    private String productCode(Product product){
        return "DAYU-%04d".formatted(product.getId());
    }
}
