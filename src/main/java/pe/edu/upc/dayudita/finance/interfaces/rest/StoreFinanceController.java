package pe.edu.upc.dayudita.finance.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.finance.application.AccountStatementService;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.PaymentService;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.*;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/finance")
@PreAuthorize("hasRole('STORE_ADMIN')")
public class StoreFinanceController {

    private final AccountStatementService accountStatementService;
    private final CreditPlanService creditPlanService;
    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public StoreFinanceController(
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

    @PostMapping("/clients/{clientId}/statements")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountStatementResponse generateStatement(
            @PathVariable Long storeId,
            @PathVariable Long clientId,
            @Valid @RequestBody GenerateStatementRequest request
    ){
        return toStatementResponse(
                accountStatementService.generateStatement(storeId, clientId, request.cutoffDate())
        );
    }

    @GetMapping("/clients/{clientId}/statements")
    public List<AccountStatementResponse> getStatements(
            @PathVariable Long storeId,
            @PathVariable Long clientId
    ){
        return accountStatementService.getStatementsForStoreClient(storeId, clientId)
                .stream()
                .map(this::toStatementResponse)
                .toList();
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
                .map(this::toCreditPlanResponse)
                .toList();
    }

    @PostMapping("/statements/{statementId}/pay")
    public PaymentResponse payStatement(
            @PathVariable Long storeId,
            @PathVariable Long statementId,
            @Valid @RequestBody RegisterPaymentRequest request
    ){
        return toPaymentResponse(
                paymentService.payStatement(storeId, statementId, request.paymentDate())
        );
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
