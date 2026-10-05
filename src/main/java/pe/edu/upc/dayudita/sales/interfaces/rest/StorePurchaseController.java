package pe.edu.upc.dayudita.sales.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.CreditPlanResponse;
import pe.edu.upc.dayudita.sales.application.PurchaseService;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/purchases")
@PreAuthorize("hasRole('STORE_ADMIN')")
public class StorePurchaseController {

    private final PurchaseService purchaseService;

    public StorePurchaseController(PurchaseService purchaseService){
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<PurchaseResponse> getPurchases(@PathVariable Long storeId){
        return purchaseService.getPurchasesByStore(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse createPurchase(
            @PathVariable Long storeId,
            @Valid @RequestBody CreatePurchaseRequest request
    ){
        return toResponse(purchaseService.createPurchase(storeId, request));
    }

    @PostMapping("/simulate")
    public CreditPlanResponse simulatePurchase(
            @PathVariable Long storeId,
            @Valid @RequestBody CreatePurchaseRequest request
    ){
        return purchaseService.simulateInstallmentPurchase(storeId, request);
    }

    private PurchaseResponse toResponse(Purchase purchase){
        List<PurchaseItemResponse> items = purchase.getDetails()
                .stream()
                .map(detail -> new PurchaseItemResponse(
                        detail.getProduct().getId(),
                        detail.getProduct().getName(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        detail.getSubtotal()
                ))
                .toList();

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getClientAccount().getClient().getId(),
                purchase.getClientAccount().getStore().getId(),
                purchase.getPurchaseDate(),
                purchase.getPaymentMode(),
                purchase.getStatus(),
                purchase.getTotal(),
                items
        );
    }
}
