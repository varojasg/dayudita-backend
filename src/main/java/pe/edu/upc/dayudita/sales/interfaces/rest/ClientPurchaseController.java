package pe.edu.upc.dayudita.sales.interfaces.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.sales.application.PurchaseService;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.PurchaseItemResponse;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.PurchaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/clients/me/purchases")
@PreAuthorize("hasRole('CLIENT')")
public class ClientPurchaseController {

    private final PurchaseService purchaseService;
    private final CurrentUserService currentUserService;

    public ClientPurchaseController(
            PurchaseService purchaseService,
            CurrentUserService currentUserService
    ){
        this.purchaseService = purchaseService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<PurchaseResponse> getMyPurchases(){
        Long clientId = currentUserService.getCurrentClientId();

        return purchaseService.getPurchasesByClient(clientId)
                .stream()
                .map(this::toResponse)
                .toList();
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
                purchase.getAnnualEffectiveRate(),
                purchase.getInstallmentCount(),
                items
        );
    }
}
