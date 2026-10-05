package pe.edu.upc.dayudita.finance.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.PaymentService;
import pe.edu.upc.dayudita.finance.domain.model.Payment;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.CreditPlanResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.PaymentResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.RegisterPaymentRequest;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/finance")
@PreAuthorize("hasRole('STORE_ADMIN')")
public class StoreFinanceController {

    private final CreditPlanService creditPlanService;
    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public StoreFinanceController(
            CreditPlanService creditPlanService,
            PaymentService paymentService,
            CurrentUserService currentUserService
    ){
        this.creditPlanService = creditPlanService;
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/clients/{clientId}/credit-plans")
    public List<CreditPlanResponse> getCreditPlans(
            @PathVariable Long storeId,
            @PathVariable Long clientId
    ){
        currentUserService.validateStoreAdmin(storeId);

        return creditPlanService.getPlansByClient(clientId)
                .stream()
                .filter(plan -> plan.getPurchase().getClientAccount().getStore().getId().equals(storeId))
                .map(creditPlanService::toCreditPlanResponse)
                .toList();
    }

    @PostMapping("/installments/{installmentId}/pay")
    public PaymentResponse payInstallment(
            @PathVariable Long storeId,
            @PathVariable Long installmentId,
            @Valid @RequestBody RegisterPaymentRequest request
    ){
        return toPaymentResponse(
                paymentService.payInstallment(storeId, installmentId, request.paymentDate())
        );
    }

    private PaymentResponse toPaymentResponse(Payment payment){
        return new PaymentResponse(
                payment.getId(),
                payment.getInstallment().getId(),
                payment.getPaymentDate(),
                payment.getTeaMoratoriaPactada(),
                payment.getDiasMora(),
                payment.getAmortizacion(),
                payment.getInteres(),
                payment.getInteresMoratorio(),
                payment.getTotalAmount()
        );
    }
}
