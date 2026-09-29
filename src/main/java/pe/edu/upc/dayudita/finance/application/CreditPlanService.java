package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.domain.repository.CreditPlanRepository;
import pe.edu.upc.dayudita.finance.domain.repository.InstallmentRepository;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CreditPlanService {

    public static final int PAYMENT_PERIOD_DAYS = 15;

    private final CreditPlanRepository creditPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final FinancialCalculator financialCalculator;
    private final FinancialDateService financialDateService;

    public CreditPlanService(
            CreditPlanRepository creditPlanRepository,
            InstallmentRepository installmentRepository,
            FinancialConfigurationService financialConfigurationService,
            FinancialCalculator financialCalculator,
            FinancialDateService financialDateService
    ){
        this.creditPlanRepository = creditPlanRepository;
        this.installmentRepository = installmentRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.financialCalculator = financialCalculator;
        this.financialDateService = financialDateService;
    }

    @Transactional
    public CreditPlan createPlan(Purchase purchase, Integer installmentCount){
        return createPlan(
                purchase,
                installmentCount,
                financialConfigurationService.getConfiguration().getAnnualEffectiveRate()
        );
    }

    @Transactional
    public CreditPlan createPlan(
            Purchase purchase,
            Integer installmentCount,
            BigDecimal annualEffectiveRate
    ){
        if(installmentCount == null || installmentCount < 1){
            throw new IllegalArgumentException("La cantidad de cuotas debe ser mayor a 0");
        }

        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();
        BigDecimal selectedAnnualRate = annualEffectiveRate == null
                ? configuration.getAnnualEffectiveRate()
                : annualEffectiveRate;

        LocalDate graceEndDate = financialDateService.getGraceEndDate(
                purchase.getPurchaseDate(),
                purchase.getClientAccount().getCutoffDay(),
                purchase.getClientAccount().getPaymentDay()
        );

        int graceDays = financialDateService.commercialDaysBetween(
                purchase.getPurchaseDate(),
                graceEndDate
        );

        BigDecimal graceRate = financialCalculator.effectiveRateForDays(
                selectedAnnualRate,
                graceDays
        );
        BigDecimal capitalizedPrincipal = financialCalculator.capitalize(
                purchase.getTotal(),
                graceRate
        );

        BigDecimal periodRate = financialCalculator.effectiveRateForDays(
                selectedAnnualRate,
                PAYMENT_PERIOD_DAYS
        );

        BigDecimal regularPayment = financialCalculator.frenchPayment(
                capitalizedPrincipal,
                periodRate,
                installmentCount
        );

        CreditPlan creditPlan = new CreditPlan();
        creditPlan.setPurchase(purchase);
        creditPlan.setPrincipal(purchase.getTotal());
        creditPlan.setCapitalizedPrincipal(capitalizedPrincipal);
        creditPlan.setAnnualEffectiveRate(selectedAnnualRate);
        creditPlan.setPeriodRate(periodRate);
        creditPlan.setGraceDays(graceDays);
        creditPlan.setInstallmentCount(installmentCount);
        creditPlan.setGraceEndDate(graceEndDate);

        creditPlan = creditPlanRepository.save(creditPlan);

        BigDecimal balance = capitalizedPrincipal;

        for(int number = 1; number <= installmentCount; number++){
            BigDecimal openingBalance = balance;

            BigDecimal interest = financialCalculator.installmentInterest(openingBalance, periodRate);
            BigDecimal amortization;
            BigDecimal payment;
            BigDecimal remainingBalance;

            if(number == installmentCount){
                amortization = financialCalculator.money(openingBalance);
                payment = financialCalculator.money(amortization.add(interest));
                remainingBalance = financialCalculator.money(BigDecimal.ZERO);
            }else{
                amortization = financialCalculator.installmentAmortization(regularPayment, interest);

                if(amortization.compareTo(BigDecimal.ZERO) <= 0){
                    throw new IllegalArgumentException("La cuota calculada no permite amortizar el capital");
                }

                payment = regularPayment;
                remainingBalance = financialCalculator.money(openingBalance.subtract(amortization));
            }

            Installment installment = new Installment();
            installment.setCreditPlan(creditPlan);
            installment.setInstallmentNumber(number);
            installment.setDueDate(financialDateService.addPaymentPeriods(
                    graceEndDate,
                    number,
                    PAYMENT_PERIOD_DAYS
            ));
            installment.setOpeningBalance(openingBalance);
            installment.setInterest(interest);
            installment.setAmortization(amortization);
            installment.setAmount(payment);
            installment.setRemainingBalance(remainingBalance);

            installmentRepository.save(installment);
            balance = remainingBalance;
        }

        return creditPlan;
    }


    public BigDecimal getGraceRate(CreditPlan plan){
        return financialCalculator.effectiveRateForDays(
                plan.getAnnualEffectiveRate(),
                plan.getGraceDays()
        );
    }

    public BigDecimal getGraceInterest(CreditPlan plan){
        return financialCalculator.money(
                plan.getCapitalizedPrincipal().subtract(plan.getPrincipal())
        );
    }

    public List<CreditPlan> getPlansByClient(Long clientId){
        return creditPlanRepository.findByPurchase_ClientAccount_Client_IdOrderByCreatedAtDesc(clientId);
    }

    public CreditPlan getPlanByPurchase(Long purchaseId){
        return creditPlanRepository.findByPurchase_Id(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("La compra no tiene un plan de cuotas"));
    }

    public List<Installment> getInstallments(Long creditPlanId){
        return installmentRepository.findByCreditPlan_IdOrderByInstallmentNumberAsc(creditPlanId);
    }


    public int getOverdueDays(Installment installment){
        if(installment.getStatus() != InstallmentStatus.PENDING || !LocalDate.now().isAfter(installment.getDueDate())){
            return 0;
        }

        return financialDateService.commercialDaysBetween(installment.getDueDate(), LocalDate.now());
    }

    public BigDecimal getCurrentMoratoryInterest(Installment installment){
        int overdueDays = getOverdueDays(installment);
        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();
        return financialCalculator.moratoryInterest(
                installment.getAmount(),
                configuration.getMoratoryAnnualEffectiveRate(),
                overdueDays
        );
    }

    public BigDecimal getCurrentAmountWithMoratory(Installment installment){
        return financialCalculator.money(
                installment.getAmount().add(getCurrentMoratoryInterest(installment))
        );
    }

    public boolean hasActivePlanForClient(Long clientId){
        return creditPlanRepository
                .findFirstByPurchase_ClientAccount_Client_IdAndStatus(clientId, CreditPlanStatus.ACTIVE)
                .isPresent();
    }
}
