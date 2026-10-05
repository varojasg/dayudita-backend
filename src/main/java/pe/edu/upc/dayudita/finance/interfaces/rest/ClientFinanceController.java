package pe.edu.upc.dayudita.finance.interfaces.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.PaymentService;
import pe.edu.upc.dayudita.finance.domain.model.Payment;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.CreditPlanResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.PaymentResponse;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;

import java.util.List;

@RestController
@RequestMapping("/api/clients/me/finance")
@PreAuthorize("hasRole('CLIENT')")
public class ClientFinanceController {

    private final CreditPlanService creditPlanService;
    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public ClientFinanceController(
            CreditPlanService creditPlanService,
            PaymentService paymentService,
            CurrentUserService currentUserService
    ){
        this.creditPlanService = creditPlanService;
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/credit-plans")
    public List<CreditPlanResponse> getMyCreditPlans(){
        Long clientId = currentUserService.getCurrentClientId();

        return creditPlanService.getPlansByClient(clientId)
                .stream()
                .map(creditPlanService::toCreditPlanResponse)
                .toList();
    }

    @GetMapping("/payments")
    public List<PaymentResponse> getMyPayments(){
        Long clientId = currentUserService.getCurrentClientId();

        return paymentService.getPaymentsByClient(clientId)
                .stream()
                .map(this::toPaymentResponse)
                .toList();
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
