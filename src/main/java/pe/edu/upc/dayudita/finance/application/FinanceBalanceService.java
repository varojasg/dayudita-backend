package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import pe.edu.upc.dayudita.finance.domain.model.CreditPlan;
import pe.edu.upc.dayudita.finance.domain.model.CreditPlanStatus;
import pe.edu.upc.dayudita.finance.domain.model.Installment;
import pe.edu.upc.dayudita.finance.domain.model.InstallmentStatus;
import pe.edu.upc.dayudita.finance.domain.repository.CreditPlanRepository;
import pe.edu.upc.dayudita.finance.domain.repository.InstallmentRepository;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class FinanceBalanceService {

    private final PurchaseRepository purchaseRepository;
    private final CreditPlanRepository creditPlanRepository;
    private final InstallmentRepository installmentRepository;

    public FinanceBalanceService(
            PurchaseRepository purchaseRepository,
            CreditPlanRepository creditPlanRepository,
            InstallmentRepository installmentRepository
    ){
        this.purchaseRepository = purchaseRepository;
        this.creditPlanRepository = creditPlanRepository;
        this.installmentRepository = installmentRepository;
    }

    public BigDecimal getOutstandingCapital(Long clientId){
        BigDecimal singlePaymentCapital = purchaseRepository
                .findByClientAccount_Client_IdAndPaymentModeAndStatusIn(
                        clientId,
                        PurchasePaymentMode.SINGLE_PAYMENT,
                        Set.of(PurchaseStatus.OPEN, PurchaseStatus.FINANCED)
                )
                .stream()
                .map(Purchase::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal installmentCapital = creditPlanRepository
                .findFirstByPurchase_ClientAccount_Client_IdAndStatus(clientId, CreditPlanStatus.ACTIVE)
                .map(plan -> installmentRepository
                        .findByCreditPlan_IdOrderByInstallmentNumberAsc(plan.getId())
                        .stream()
                        .filter(installment -> installment.getStatus() == InstallmentStatus.PENDING)
                        .map(Installment::getAmortization)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);

        return singlePaymentCapital.add(installmentCapital);
    }

    public boolean hasOutstandingCreditAtOtherStore(Long clientId, Long storeId){
        List<Purchase> singlePaymentPurchases = purchaseRepository
                .findByClientAccount_Client_IdAndPaymentModeAndStatusIn(
                        clientId,
                        PurchasePaymentMode.SINGLE_PAYMENT,
                        Set.of(PurchaseStatus.OPEN, PurchaseStatus.FINANCED)
                );

        boolean singlePaymentInOtherStore = singlePaymentPurchases.stream()
                .anyMatch(purchase -> !purchase.getClientAccount().getStore().getId().equals(storeId));

        boolean installmentInOtherStore = creditPlanRepository
                .findFirstByPurchase_ClientAccount_Client_IdAndStatus(clientId, CreditPlanStatus.ACTIVE)
                .map(CreditPlan::getPurchase)
                .map(Purchase::getClientAccount)
                .map(account -> !account.getStore().getId().equals(storeId))
                .orElse(false);

        return singlePaymentInOtherStore || installmentInOtherStore;
    }
}
