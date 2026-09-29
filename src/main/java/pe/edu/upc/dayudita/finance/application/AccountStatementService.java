package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.domain.repository.AccountStatementRepository;
import pe.edu.upc.dayudita.finance.domain.repository.StatementItemRepository;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccountStatementService {

    private final AccountStatementRepository accountStatementRepository;
    private final StatementItemRepository statementItemRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final PurchaseRepository purchaseRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final FinancialCalculator financialCalculator;
    private final FinancialDateService financialDateService;
    private final CurrentUserService currentUserService;

    public AccountStatementService(
            AccountStatementRepository accountStatementRepository,
            StatementItemRepository statementItemRepository,
            ClientAccountRepository clientAccountRepository,
            PurchaseRepository purchaseRepository,
            FinancialConfigurationService financialConfigurationService,
            FinancialCalculator financialCalculator,
            FinancialDateService financialDateService,
            CurrentUserService currentUserService
    ){
        this.accountStatementRepository = accountStatementRepository;
        this.statementItemRepository = statementItemRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.purchaseRepository = purchaseRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.financialCalculator = financialCalculator;
        this.financialDateService = financialDateService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AccountStatement generateStatement(Long storeId, Long clientId, LocalDate cutoffDate){
        currentUserService.validateStoreAdmin(storeId);

        if(cutoffDate.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("La fecha de corte no puede ser futura");
        }

        ClientAccount account = clientAccountRepository.findByClient_IdAndStore_Id(clientId, storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        if(!financialDateService.matchesContractualDay(cutoffDate, account.getCutoffDay())){
            throw new IllegalArgumentException("La fecha indicada no corresponde al dia de corte del cliente");
        }

        if(accountStatementRepository.existsByClientAccount_IdAndCutoffDate(account.getId(), cutoffDate)){
            throw new IllegalArgumentException("El estado de cuenta de este corte ya fue generado");
        }

        List<Purchase> purchases = purchaseRepository
                .findByClientAccount_IdAndPaymentModeAndStatusAndPurchaseDateLessThanEqualOrderByPurchaseDateAsc(
                        account.getId(),
                        PurchasePaymentMode.SINGLE_PAYMENT,
                        PurchaseStatus.OPEN,
                        cutoffDate
                );

        if(purchases.isEmpty()){
            throw new IllegalArgumentException("No existen compras pendientes para este corte");
        }

        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();
        LocalDate dueDate = financialDateService.getPaymentDateForCutoff(
                cutoffDate,
                account.getCutoffDay(),
                account.getPaymentDay()
        );

        AccountStatement statement = new AccountStatement();
        statement.setClientAccount(account);
        statement.setCutoffDate(cutoffDate);
        statement.setDueDate(dueDate);
        statement.setAnnualEffectiveRate(configuration.getAnnualEffectiveRate());
        statement.setPrincipal(BigDecimal.ZERO);
        statement.setCompensatoryInterest(BigDecimal.ZERO);
        statement.setTotalAmount(BigDecimal.ZERO);

        statement = accountStatementRepository.save(statement);

        BigDecimal principalTotal = BigDecimal.ZERO;
        BigDecimal interestTotal = BigDecimal.ZERO;

        for(Purchase purchase : purchases){
            int financedDays = financialDateService.commercialDaysBetween(
                    purchase.getPurchaseDate(),
                    dueDate
            );

            BigDecimal purchaseAnnualRate = purchase.getAnnualEffectiveRate() == null
                    ? configuration.getAnnualEffectiveRate()
                    : purchase.getAnnualEffectiveRate();

            BigDecimal periodRate = financialCalculator.effectiveRateForDays(
                    purchaseAnnualRate,
                    financedDays
            );
            BigDecimal interest = financialCalculator.money(
                    purchase.getTotal().multiply(periodRate)
            );
            BigDecimal total = financialCalculator.money(
                    purchase.getTotal().add(interest)
            );

            StatementItem item = new StatementItem();
            item.setStatement(statement);
            item.setPurchase(purchase);
            item.setPrincipal(purchase.getTotal());
            item.setCompensatoryInterest(interest);
            item.setTotalAmount(total);
            statementItemRepository.save(item);

            purchase.setStatus(PurchaseStatus.FINANCED);
            purchaseRepository.save(purchase);

            principalTotal = principalTotal.add(purchase.getTotal());
            interestTotal = interestTotal.add(interest);
        }

        statement.setPrincipal(financialCalculator.money(principalTotal));
        statement.setCompensatoryInterest(financialCalculator.money(interestTotal));
        statement.setTotalAmount(financialCalculator.money(principalTotal.add(interestTotal)));

        return accountStatementRepository.save(statement);
    }

    public List<AccountStatement> getStatementsForStoreClient(Long storeId, Long clientId){
        currentUserService.validateStoreAdmin(storeId);
        return accountStatementRepository
                .findByClientAccount_Store_IdAndClientAccount_Client_IdOrderByCutoffDateDesc(storeId, clientId);
    }

    public List<AccountStatement> getStatementsByClient(Long clientId){
        return accountStatementRepository.findByClientAccount_Client_IdOrderByCutoffDateDesc(clientId);
    }

    public List<StatementItem> getItems(Long statementId){
        return statementItemRepository.findByStatement_IdOrderByPurchase_PurchaseDateAsc(statementId);
    }

    public int getOverdueDays(AccountStatement statement){
        if(statement.getStatus() != StatementStatus.OPEN || !LocalDate.now().isAfter(statement.getDueDate())){
            return 0;
        }

        return financialDateService.commercialDaysBetween(statement.getDueDate(), LocalDate.now());
    }

    public BigDecimal getCurrentMoratoryInterest(AccountStatement statement){
        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();
        return financialCalculator.moratoryInterest(
                statement.getTotalAmount(),
                configuration.getMoratoryAnnualEffectiveRate(),
                getOverdueDays(statement)
        );
    }

    public BigDecimal getCurrentAmountWithMoratory(AccountStatement statement){
        return financialCalculator.money(
                statement.getTotalAmount().add(getCurrentMoratoryInterest(statement))
        );
    }
}
