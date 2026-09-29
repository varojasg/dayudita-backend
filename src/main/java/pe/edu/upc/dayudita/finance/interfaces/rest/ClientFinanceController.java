package pe.edu.upc.dayudita.finance.interfaces.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.dayudita.finance.application.AccountStatementService;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.PaymentService;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.*;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;

import java.util.List;

@RestController
@RequestMapping("/api/clients/me/finance")
@PreAuthorize("hasRole('CLIENT')")
public class ClientFinanceController {

    private final AccountStatementService accountStatementService;
    private final CreditPlanService creditPlanService;
    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public ClientFinanceController(
            AccountStatementService accountStatementService,
            CreditPlanService creditPlanService,
            PaymentService paymentService,
            CurrentUserService currentUserService
    ){
        this.accountStatementService = accountStatementService;
        this.creditPlanService = creditPlanService;
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/statements")
    public List<AccountStatementResponse> getMyStatements(){
        Long clientId = currentUserService.getCurrentClientId();

        return accountStatementService.getStatementsByClient(clientId)
                .stream()
                .map(this::toStatementResponse)
                .toList();
    }

    @GetMapping("/credit-plans")
    public List<CreditPlanResponse> getMyCreditPlans(){
        Long clientId = currentUserService.getCurrentClientId();

        return creditPlanService.getPlansByClient(clientId)
                .stream()
                .map(this::toCreditPlanResponse)
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

    private AccountStatementResponse toStatementResponse(AccountStatement statement){
        List<StatementItemResponse> items = accountStatementService.getItems(statement.getId())
                .stream()
                .map(item -> new StatementItemResponse(
                        item.getPurchase().getId(),
                        item.getPurchase().getPurchaseDate(),
                        item.getPrincipal(),
                        item.getCompensatoryInterest(),
                        item.getTotalAmount()
                ))
                .toList();

        return new AccountStatementResponse(
                statement.getId(),
                statement.getClientAccount().getClient().getId(),
                statement.getClientAccount().getStore().getId(),
                statement.getCutoffDate(),
                statement.getDueDate(),
                statement.getAnnualEffectiveRate(),
                statement.getPrincipal(),
                statement.getCompensatoryInterest(),
                statement.getTotalAmount(),
                statement.getStatus(),
                statement.getPaymentDate(),
                accountStatementService.getOverdueDays(statement),
                accountStatementService.getCurrentMoratoryInterest(statement),
                accountStatementService.getCurrentAmountWithMoratory(statement),
                items
        );
    }

    private CreditPlanResponse toCreditPlanResponse(CreditPlan plan){
        List<InstallmentResponse> installments = creditPlanService.getInstallments(plan.getId())
                .stream()
                .map(this::toInstallmentResponse)
                .toList();

        return new CreditPlanResponse(
                plan.getId(),
                plan.getPurchase().getId(),
                plan.getPurchase().getClientAccount().getClient().getId(),
                plan.getPurchase().getClientAccount().getStore().getId(),
                plan.getPrincipal(),
                plan.getCapitalizedPrincipal(),
                plan.getAnnualEffectiveRate(),
                creditPlanService.getGraceRate(plan),
                creditPlanService.getGraceInterest(plan),
                plan.getPeriodRate(),
                CreditPlanService.PAYMENT_PERIOD_DAYS,
                plan.getGraceDays(),
                plan.getInstallmentCount(),
                plan.getGraceEndDate(),
                plan.getStatus(),
                installments
        );
    }

    private InstallmentResponse toInstallmentResponse(Installment installment){
        return new InstallmentResponse(
                installment.getId(),
                installment.getInstallmentNumber(),
                installment.getDueDate(),
                installment.getOpeningBalance(),
                installment.getInterest(),
                installment.getAmortization(),
                installment.getAmount(),
                installment.getRemainingBalance(),
                installment.getStatus(),
                installment.getPaymentDate(),
                creditPlanService.getOverdueDays(installment),
                creditPlanService.getCurrentMoratoryInterest(installment),
                creditPlanService.getCurrentAmountWithMoratory(installment)
        );
    }

    private PaymentResponse toPaymentResponse(Payment payment){
        return new PaymentResponse(
                payment.getId(),
                payment.getType(),
                payment.getStatement() != null ? payment.getStatement().getId() : null,
                payment.getInstallment() != null ? payment.getInstallment().getId() : null,
                payment.getPaymentDate(),
                payment.getMoratoryAnnualEffectiveRate(),
                payment.getPrincipalAmount(),
                payment.getCompensatoryInterest(),
                payment.getMoratoryInterest(),
                payment.getTotalAmount()
        );
    }
}
