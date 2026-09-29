package pe.edu.upc.dayudita.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.clients.domain.repository.ClientRepository;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.FinancialCalculator;
import pe.edu.upc.dayudita.finance.application.FinancialDateService;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.domain.repository.*;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;
import pe.edu.upc.dayudita.iam.domain.repository.AdministratorRepository;
import pe.edu.upc.dayudita.products.domain.model.Product;
import pe.edu.upc.dayudita.products.domain.repository.ProductRepository;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseDetail;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name = "dayudita.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer {

    private static final String PASSWORD = "Dayudita123";

    private final AdministratorRepository administratorRepository;
    private final ClientRepository clientRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final CreditPlanRepository creditPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final AccountStatementRepository accountStatementRepository;
    private final StatementItemRepository statementItemRepository;
    private final PaymentRepository paymentRepository;
    private final FinancialConfigurationRepository financialConfigurationRepository;
    private final CreditPlanService creditPlanService;
    private final FinancialCalculator financialCalculator;
    private final FinancialDateService financialDateService;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(
            AdministratorRepository administratorRepository,
            ClientRepository clientRepository,
            ClientAccountRepository clientAccountRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository,
            PurchaseRepository purchaseRepository,
            CreditPlanRepository creditPlanRepository,
            InstallmentRepository installmentRepository,
            AccountStatementRepository accountStatementRepository,
            StatementItemRepository statementItemRepository,
            PaymentRepository paymentRepository,
            FinancialConfigurationRepository financialConfigurationRepository,
            CreditPlanService creditPlanService,
            FinancialCalculator financialCalculator,
            FinancialDateService financialDateService,
            PasswordEncoder passwordEncoder
    ){
        this.administratorRepository = administratorRepository;
        this.clientRepository = clientRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.creditPlanRepository = creditPlanRepository;
        this.installmentRepository = installmentRepository;
        this.accountStatementRepository = accountStatementRepository;
        this.statementItemRepository = statementItemRepository;
        this.paymentRepository = paymentRepository;
        this.financialConfigurationRepository = financialConfigurationRepository;
        this.creditPlanService = creditPlanService;
        this.financialCalculator = financialCalculator;
        this.financialDateService = financialDateService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize(){
        createFinancialConfiguration();
        createAdministrator("Valeria", "Gomez", "system@dayudita.pe", AdministratorRole.SYSTEM_ADMIN, null);

        Store centro = createStore("Dayu Centro", "Av. Arequipa 2450, Lince", "987111111");
        Store miraflores = createStore("Dayu Miraflores", "Av. Larco 650, Miraflores", "987222222");
        Store sanIsidro = createStore("Dayu San Isidro", "Av. Salaverry 1880, San Isidro", "987333333");

        createAdministrator("Marko", "Rojas", "admin@dayudita.pe", AdministratorRole.STORE_ADMIN, centro);
        createAdministrator("Oscar", "Salazar", "oscar@dayudita.pe", AdministratorRole.STORE_ADMIN, miraflores);
        createAdministrator("Winnie", "Chen", "winnie@dayudita.pe", AdministratorRole.STORE_ADMIN, sanIsidro);

        List<Product> centroProducts = createProducts(centro);
        List<Product> mirafloresProducts = createProducts(miraflores);
        List<Product> sanIsidroProducts = createProducts(sanIsidro);

        ClientAccount briguitte = createClientAccount(
                centro, "Briguitte", "Sanchez", "70000001", "cliente@dayudita.pe", "999111222", 15, 30
        );
        ClientAccount cesar = createClientAccount(
                centro, "Cesar", "Mendoza", "70000002", "cesar@dayudita.pe", "999222333", 12, 27
        );
        ClientAccount fabiana = createClientAccount(
                miraflores, "Fabiana", "Lopez", "70000003", "fabiana@dayudita.pe", "999333444", 10, 25
        );
        ClientAccount bianca = createClientAccount(
                sanIsidro, "Bianca", "Torres", "70000004", "bianca@dayudita.pe", "999444555", 20, 5
        );

        if(purchaseRepository.findByClientAccount_Client_IdOrderByPurchaseDateDesc(briguitte.getClient().getId()).isEmpty()){
            createInstallmentHistory(briguitte, centroProducts.get(5), LocalDate.now().minusDays(150), 4);
            createStatement(briguitte, centroProducts.get(6), LocalDate.now().minusDays(95), true);
            createActivePlan(briguitte, centroProducts.get(0), LocalDate.now().minusDays(20), 6, 3);
        }

        if(purchaseRepository.findByClientAccount_Client_IdOrderByPurchaseDateDesc(cesar.getClient().getId()).isEmpty()){
            createInstallmentHistory(cesar, centroProducts.get(1), LocalDate.now().minusDays(180), 3);
            createStatement(cesar, centroProducts.get(4), LocalDate.now().minusDays(70), false);
        }

        if(purchaseRepository.findByClientAccount_Client_IdOrderByPurchaseDateDesc(fabiana.getClient().getId()).isEmpty()){
            createStatement(fabiana, mirafloresProducts.get(7), LocalDate.now().minusDays(140), true);
            createActivePlan(fabiana, mirafloresProducts.get(2), LocalDate.now().minusDays(55), 8, 2);
        }

        if(purchaseRepository.findByClientAccount_Client_IdOrderByPurchaseDateDesc(bianca.getClient().getId()).isEmpty()){
            createInstallmentHistory(bianca, sanIsidroProducts.get(3), LocalDate.now().minusDays(220), 5);
            createStatement(bianca, sanIsidroProducts.get(5), LocalDate.now().minusDays(125), true);
        }
    }

    private void createFinancialConfiguration(){
        FinancialConfiguration configuration = financialConfigurationRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(FinancialConfiguration::new);

        if(configuration.getId() == null){
            configuration.setMinAnnualEffectiveRate(new BigDecimal("0.05"));
            configuration.setMaxAnnualEffectiveRate(new BigDecimal("0.80"));
            configuration.setAnnualEffectiveRate(new BigDecimal("0.24"));
            configuration.setMoratoryAnnualEffectiveRate(new BigDecimal("0.35"));
            configuration.setMinCapital(new BigDecimal("20.00"));
            configuration.setMaxCapital(new BigDecimal("500.00"));
            configuration.setCreditLimit(new BigDecimal("500.00"));
            configuration.setMaxInstallments(12);
            financialConfigurationRepository.save(configuration);
            return;
        }

        boolean legacyDefaults = new BigDecimal("1500.00").compareTo(configuration.getMaxCapital()) == 0
                && new BigDecimal("2000.00").compareTo(configuration.getCreditLimit()) == 0;

        if(legacyDefaults){
            configuration.setMaxCapital(new BigDecimal("500.00"));
            configuration.setCreditLimit(new BigDecimal("500.00"));
            financialConfigurationRepository.save(configuration);
        }
    }

    private Store createStore(String name, String address, String phone){
        Store store = storeRepository.findByName(name).orElseGet(Store::new);
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);
        store.setActive(true);
        return storeRepository.save(store);
    }

    private void createAdministrator(
            String firstName,
            String lastName,
            String email,
            AdministratorRole role,
            Store store
    ){
        Administrator administrator;

        if(role == AdministratorRole.STORE_ADMIN && store != null){
            administrator = administratorRepository.findByEmailIgnoreCase(email)
                    .or(() -> administratorRepository.findByStore_Id(store.getId()))
                    .orElseGet(Administrator::new);
        } else {
            administrator = administratorRepository.findByEmailIgnoreCase(email)
                    .or(() -> administratorRepository.findFirstByRole(role))
                    .orElseGet(Administrator::new);
        }

        administrator.setFirstName(firstName);
        administrator.setLastName(lastName);
        administrator.setEmail(email);
        administrator.setPassword(passwordEncoder.encode(PASSWORD));
        administrator.setRole(role);
        administrator.setStore(store);
        administrator.setActive(true);
        administratorRepository.save(administrator);
    }

    private ClientAccount createClientAccount(
            Store store,
            String firstName,
            String lastName,
            String document,
            String email,
            String phone,
            int cutoffDay,
            int paymentDay
    ){
        Client client = clientRepository.findByEmailIgnoreCase(email)
                .or(() -> clientRepository.findByDocumentNumber(document))
                .orElseGet(Client::new);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setDocumentNumber(document);
        client.setEmail(email);
        client.setPassword(passwordEncoder.encode(PASSWORD));
        client.setPhone(phone);
        client.setActive(true);
        client = clientRepository.save(client);

        ClientAccount account = clientAccountRepository.findByClient_IdAndStore_Id(client.getId(), store.getId())
                .orElseGet(ClientAccount::new);
        account.setClient(client);
        account.setStore(store);
        account.setCutoffDay(cutoffDay);
        account.setPaymentDay(paymentDay);
        account.setActive(true);
        return clientAccountRepository.save(account);
    }

    private List<Product> createProducts(Store store){
        return List.of(
                product(store, "Piñata unicornio", "Piñata grande de unicornio para cumpleaños y celebraciones", "85.00", "95.00"),
                product(store, "Pack de 200 globos", "Pack surtido de 200 globos para decoración de fiestas", "72.00", "82.00"),
                product(store, "Pack de 12 globos temáticos", "Doce globos temáticos para complementar la decoración", "48.00", "56.00"),
                product(store, "Pack de 12 peluches", "Doce peluches pequeños para regalos, premios o decoración", "118.00", "132.00"),
                product(store, "Pack carpa cumpleañera", "Set decorativo tipo carpa para mesa principal de cumpleaños", "145.00", "160.00"),
                product(store, "Pack premios para piñata", "Surtido de premios pequeños para rellenar la piñata", "55.00", "65.00"),
                product(store, "Pack utensilios para comer", "Vasos, platos y utensilios desechables para una celebración", "60.00", "70.00"),
                product(store, "Estante para postres", "Estante decorativo para organizar dulces y postres de la mesa", "135.00", "150.00")
        );
    }

    private Product product(Store store, String name, String description, String cashPrice, String creditPrice){
        Product product = productRepository.findByStore_IdAndName(store.getId(), name).orElseGet(Product::new);
        product.setStore(store);
        product.setName(name);
        product.setSupplier("Dayu");
        product.setBrand("Dayu");
        product.setDescription(description);
        product.setUnitOfMeasure("unidad");
        product.setCashPrice(new BigDecimal(cashPrice));
        product.setCreditPrice(new BigDecimal(creditPrice));
        product.setAllowsSinglePayment(true);
        product.setAllowsInstallments(true);
        product.setActive(true);
        return productRepository.save(product);
    }

    private void createActivePlan(
            ClientAccount account,
            Product product,
            LocalDate purchaseDate,
            int installments,
            int quantity
    ){
        Purchase purchase = createPurchase(account, product, purchaseDate, PurchasePaymentMode.INSTALLMENTS, installments, quantity);
        creditPlanService.createPlan(purchase, installments);
    }

    private void createInstallmentHistory(
            ClientAccount account,
            Product product,
            LocalDate purchaseDate,
            int installments
    ){
        Purchase purchase = createPurchase(account, product, purchaseDate, PurchasePaymentMode.INSTALLMENTS, installments, 1);
        CreditPlan plan = creditPlanService.createPlan(purchase, installments);
        List<Installment> planInstallments = installmentRepository.findByCreditPlan_IdOrderByInstallmentNumberAsc(plan.getId());

        for(Installment installment : planInstallments){
            installment.setStatus(InstallmentStatus.PAID);
            installment.setPaymentDate(installment.getDueDate());
            installmentRepository.save(installment);
        }

        plan.setStatus(CreditPlanStatus.PAID);
        creditPlanRepository.save(plan);
        purchase.setStatus(PurchaseStatus.PAID);
        purchaseRepository.save(purchase);
    }

    private void createStatement(
            ClientAccount account,
            Product product,
            LocalDate purchaseDate,
            boolean paid
    ){
        Purchase purchase = createPurchase(account, product, purchaseDate, PurchasePaymentMode.SINGLE_PAYMENT, null, 1);
        LocalDate cutoffDate = financialDateService.getCutoffDate(purchaseDate, account.getCutoffDay());

        if(accountStatementRepository.existsByClientAccount_IdAndCutoffDate(account.getId(), cutoffDate)) return;

        LocalDate dueDate = financialDateService.getPaymentDateForCutoff(
                cutoffDate,
                account.getCutoffDay(),
                account.getPaymentDay()
        );
        FinancialConfiguration configuration = financialConfigurationRepository.findFirstByOrderByIdAsc().orElseThrow();
        int financedDays = financialDateService.commercialDaysBetween(purchaseDate, dueDate);
        BigDecimal rate = financialCalculator.effectiveRateForDays(configuration.getAnnualEffectiveRate(), financedDays);
        BigDecimal interest = financialCalculator.money(purchase.getTotal().multiply(rate));
        BigDecimal total = financialCalculator.money(purchase.getTotal().add(interest));

        AccountStatement statement = new AccountStatement();
        statement.setClientAccount(account);
        statement.setCutoffDate(cutoffDate);
        statement.setDueDate(dueDate);
        statement.setAnnualEffectiveRate(configuration.getAnnualEffectiveRate());
        statement.setPrincipal(purchase.getTotal());
        statement.setCompensatoryInterest(interest);
        statement.setTotalAmount(total);
        statement.setStatus(paid ? StatementStatus.PAID : StatementStatus.OPEN);
        statement.setPaymentDate(paid ? dueDate : null);
        statement = accountStatementRepository.save(statement);

        StatementItem item = new StatementItem();
        item.setStatement(statement);
        item.setPurchase(purchase);
        item.setPrincipal(purchase.getTotal());
        item.setCompensatoryInterest(interest);
        item.setTotalAmount(total);
        statementItemRepository.save(item);

        purchase.setStatus(paid ? PurchaseStatus.PAID : PurchaseStatus.FINANCED);
        purchaseRepository.save(purchase);

        if(paid){
            Payment payment = new Payment();
            payment.setType(PaymentType.STATEMENT);
            payment.setStatement(statement);
            payment.setPaymentDate(dueDate);
            payment.setMoratoryAnnualEffectiveRate(configuration.getMoratoryAnnualEffectiveRate());
            payment.setPrincipalAmount(purchase.getTotal());
            payment.setCompensatoryInterest(interest);
            payment.setMoratoryInterest(BigDecimal.ZERO.setScale(2));
            payment.setTotalAmount(total);
            paymentRepository.save(payment);
        }
    }

    private Purchase createPurchase(
            ClientAccount account,
            Product product,
            LocalDate purchaseDate,
            PurchasePaymentMode paymentMode,
            Integer installmentCount,
            int quantity
    ){
        Purchase purchase = new Purchase();
        purchase.setClientAccount(account);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setPaymentMode(paymentMode);
        purchase.setInstallmentCount(installmentCount);
        purchase.setAnnualEffectiveRate(financialConfigurationRepository.findFirstByOrderByIdAsc().orElseThrow().getAnnualEffectiveRate());
        purchase.setStatus(paymentMode == PurchasePaymentMode.INSTALLMENTS ? PurchaseStatus.FINANCED : PurchaseStatus.OPEN);
        BigDecimal unitPrice = product.getCreditPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2);
        purchase.setTotal(total);

        PurchaseDetail detail = new PurchaseDetail();
        detail.setPurchase(purchase);
        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.setSubtotal(total);
        purchase.getDetails().add(detail);

        return purchaseRepository.save(purchase);
    }
}
