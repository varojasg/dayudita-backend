package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.domain.repository.*;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PaymentService {

    private final AccountStatementRepository accountStatementRepository;
    private final StatementItemRepository statementItemRepository;
    private final CreditPlanRepository creditPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseRepository purchaseRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final FinancialCalculator financialCalculator;
    private final FinancialDateService financialDateService;
    private final CurrentUserService currentUserService;

    public PaymentService(
            AccountStatementRepository accountStatementRepository,
            StatementItemRepository statementItemRepository,
            CreditPlanRepository creditPlanRepository,
            InstallmentRepository installmentRepository,
            PaymentRepository paymentRepository,
            PurchaseRepository purchaseRepository,
            FinancialConfigurationService financialConfigurationService,
            FinancialCalculator financialCalculator,
            FinancialDateService financialDateService,
            CurrentUserService currentUserService
    ){
        this.accountStatementRepository = accountStatementRepository;
        this.statementItemRepository = statementItemRepository;
        this.creditPlanRepository = creditPlanRepository;
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.purchaseRepository = purchaseRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.financialCalculator = financialCalculator;
        this.financialDateService = financialDateService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Payment payStatement(Long storeId, Long statementId, LocalDate paymentDate){
        currentUserService.validateStoreAdmin(storeId);

        if(paymentDate.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("La fecha de pago no puede ser futura");
        }

        AccountStatement statement = accountStatementRepository.findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException("El estado de cuenta no existe"));

        if(!statement.getClientAccount().getStore().getId().equals(storeId)){
            throw new IllegalArgumentException("El estado de cuenta no pertenece a esta tienda");
        }

        if(statement.getStatus() == StatementStatus.PAID){
            throw new IllegalArgumentException("El estado de cuenta ya fue pagado");
        }

        if(paymentDate.isBefore(statement.getCutoffDate())){
            throw new IllegalArgumentException("La fecha de pago no puede ser anterior al corte");
        }

        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();
        int overdueDays = paymentDate.isAfter(statement.getDueDate())
                ? financialDateService.commercialDaysBetween(statement.getDueDate(), paymentDate)
                : 0;

        BigDecimal moratoryInterest = financialCalculator.moratoryInterest(
                statement.getTotalAmount(),
                configuration.getMoratoryAnnualEffectiveRate(),
                overdueDays
        );

        BigDecimal totalPayment = financialCalculator.money(
                statement.getTotalAmount().add(moratoryInterest)
        );

        Payment payment = new Payment();
        payment.setType(PaymentType.STATEMENT);
        payment.setStatement(statement);
        payment.setPaymentDate(paymentDate);
        payment.setMoratoryAnnualEffectiveRate(configuration.getMoratoryAnnualEffectiveRate());
        payment.setPrincipalAmount(statement.getPrincipal());
        payment.setCompensatoryInterest(statement.getCompensatoryInterest());
        payment.setMoratoryInterest(moratoryInterest);
        payment.setTotalAmount(totalPayment);
        payment = paymentRepository.save(payment);

        statement.setStatus(StatementStatus.PAID);
        statement.setPaymentDate(paymentDate);
        accountStatementRepository.save(statement);

        statementItemRepository.findByStatement_IdOrderByPurchase_PurchaseDateAsc(statementId)
                .forEach(item -> {
                    item.getPurchase().setStatus(PurchaseStatus.PAID);
                    purchaseRepository.save(item.getPurchase());
                });

        return payment;
    }

    @Transactional
    public Payment payInstallment(Long storeId, Long installmentId, LocalDate paymentDate){
        currentUserService.validateStoreAdmin(storeId);

        if(paymentDate.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("La fecha de pago no puede ser futura");
        }

        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("La cuota no existe"));

        CreditPlan plan = installment.getCreditPlan();

        if(!plan.getPurchase().getClientAccount().getStore().getId().equals(storeId)){
            throw new IllegalArgumentException("La cuota no pertenece a esta tienda");
        }

        if(installment.getStatus() == InstallmentStatus.PAID){
            throw new IllegalArgumentException("La cuota ya fue pagada");
        }

        if(installment.getInstallmentNumber() > 1){
            Installment previousInstallment = installmentRepository
                    .findByCreditPlan_IdAndInstallmentNumber(
                            plan.getId(),
                            installment.getInstallmentNumber() - 1
                    )
                    .orElseThrow(() -> new IllegalArgumentException("No se encontro la cuota anterior"));

            if(previousInstallment.getStatus() != InstallmentStatus.PAID){
                throw new IllegalArgumentException("Primero se debe pagar la cuota anterior");
            }
        }

        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();
        int overdueDays = paymentDate.isAfter(installment.getDueDate())
                ? financialDateService.commercialDaysBetween(installment.getDueDate(), paymentDate)
                : 0;

        BigDecimal moratoryInterest = financialCalculator.moratoryInterest(
                installment.getAmount(),
                configuration.getMoratoryAnnualEffectiveRate(),
                overdueDays
        );

        BigDecimal totalPayment = financialCalculator.money(
                installment.getAmount().add(moratoryInterest)
        );

        Payment payment = new Payment();
        payment.setType(PaymentType.INSTALLMENT);
        payment.setInstallment(installment);
        payment.setPaymentDate(paymentDate);
        payment.setMoratoryAnnualEffectiveRate(configuration.getMoratoryAnnualEffectiveRate());
        payment.setPrincipalAmount(installment.getAmortization());
        payment.setCompensatoryInterest(installment.getInterest());
        payment.setMoratoryInterest(moratoryInterest);
        payment.setTotalAmount(totalPayment);
        payment = paymentRepository.save(payment);

        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaymentDate(paymentDate);
        installmentRepository.save(installment);

        if(!installmentRepository.existsByCreditPlan_IdAndStatus(plan.getId(), InstallmentStatus.PENDING)){
            plan.setStatus(CreditPlanStatus.PAID);
            creditPlanRepository.save(plan);

            plan.getPurchase().setStatus(PurchaseStatus.PAID);
            purchaseRepository.save(plan.getPurchase());
        }

        return payment;
    }

    public List<Payment> getPaymentsByClient(Long clientId){
        List<Payment> payments = new ArrayList<>();
        payments.addAll(paymentRepository.findByStatement_ClientAccount_Client_IdOrderByPaymentDateDesc(clientId));
        payments.addAll(paymentRepository
                .findByInstallment_CreditPlan_Purchase_ClientAccount_Client_IdOrderByPaymentDateDesc(clientId));
        payments.sort(Comparator.comparing(Payment::getPaymentDate).reversed());
        return payments;
    }
}
