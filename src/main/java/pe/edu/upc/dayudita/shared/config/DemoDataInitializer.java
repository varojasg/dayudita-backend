package pe.edu.upc.dayudita.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.model.CreditCurrency;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.clients.domain.repository.ClientRepository;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.finance.domain.model.CreditPlanStatus;
import pe.edu.upc.dayudita.finance.domain.repository.CreditPlanRepository;
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

/**
 * Datos de demo alineados al Caso B del simulador de Excel
 * (capital 500, TEA 20%, TEA moratoria 12%, 1 periodo de gracia total y
 * 1 parcial), para poder comparar directamente la demo contra el Excel.
 */
@Component
@ConditionalOnProperty(name = "dayudita.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer {

    private static final String DEMO_PASSWORD = "Dayudita123";
    private static final String SYSTEM_ADMIN_EMAIL = "system@dayudita.pe";
    private static final String STORE_ADMIN_EMAIL = "admin@dayudita.pe";
    private static final String CLIENT_EMAIL = "cliente@dayudita.pe";
    private static final String CLIENT_DOCUMENT = "70000001";

    private final AdministratorRepository administratorRepository;
    private final ClientRepository clientRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final CreditPlanRepository creditPlanRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final CreditPlanService creditPlanService;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(
            AdministratorRepository administratorRepository,
            ClientRepository clientRepository,
            ClientAccountRepository clientAccountRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository,
            PurchaseRepository purchaseRepository,
            CreditPlanRepository creditPlanRepository,
            FinancialConfigurationService financialConfigurationService,
            CreditPlanService creditPlanService,
            PasswordEncoder passwordEncoder
    ){
        this.administratorRepository = administratorRepository;
        this.clientRepository = clientRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.creditPlanRepository = creditPlanRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.creditPlanService = creditPlanService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize(){
        createSystemAdmin();
        Store store = createStore();
        createCreditPolicy(store);
        createStoreAdmin(store);
        ClientAccount account = createClientAndAccount(store);
        List<Product> products = createProducts(store);
        createFrenchPlan(account, products.get(0));
    }

    private void createCreditPolicy(Store store){
        try {
            financialConfigurationService.getConfiguration(store.getId());
        } catch (IllegalArgumentException notFound){
            financialConfigurationService.createDefaultConfiguration(store);
        }
    }

    private void createSystemAdmin(){
        Administrator admin = administratorRepository.findByEmailIgnoreCase(SYSTEM_ADMIN_EMAIL)
                .or(() -> administratorRepository.findFirstByRole(AdministratorRole.SYSTEM_ADMIN))
                .orElseGet(Administrator::new);

        admin.setFirstName("Sistema");
        admin.setLastName("Dayu");
        admin.setEmail(SYSTEM_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        admin.setRole(AdministratorRole.SYSTEM_ADMIN);
        admin.setStore(null);
        admin.setActive(true);
        administratorRepository.save(admin);
    }

    private Store createStore(){
        Store store = storeRepository.findByName("Dayu Centro").orElseGet(Store::new);
        store.setName("Dayu Centro");
        store.setAddress("Av. Principal 123, Lima");
        store.setPhone("987654321");
        store.setActive(true);
        return storeRepository.save(store);
    }

    private void createStoreAdmin(Store store){
        Administrator admin = administratorRepository.findByEmailIgnoreCase(STORE_ADMIN_EMAIL)
                .or(() -> administratorRepository.findByStore_Id(store.getId()))
                .orElseGet(Administrator::new);

        admin.setFirstName("María");
        admin.setLastName("Dayu");
        admin.setEmail(STORE_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        admin.setRole(AdministratorRole.STORE_ADMIN);
        admin.setStore(store);
        admin.setActive(true);
        administratorRepository.save(admin);
    }

    private ClientAccount createClientAndAccount(Store store){
        Client client = clientRepository.findByEmailIgnoreCase(CLIENT_EMAIL)
                .or(() -> clientRepository.findByDocumentNumber(CLIENT_DOCUMENT))
                .orElseGet(Client::new);

        client.setFirstName("Ana");
        client.setLastName("Torres");
        client.setDocumentNumber(CLIENT_DOCUMENT);
        client.setEmail(CLIENT_EMAIL);
        client.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        client.setPhone("999111222");
        client.setActive(true);
        client = clientRepository.save(client);

        ClientAccount account = clientAccountRepository.findByClient_IdAndStore_Id(client.getId(), store.getId())
                .orElseGet(ClientAccount::new);
        account.setClient(client);
        account.setStore(store);
        account.setTeaPactada(new BigDecimal("0.20"));
        account.setTeaMoratoriaPactada(new BigDecimal("0.12"));
        account.setCurrency(CreditCurrency.PEN);
        account.setLimiteCredito(new BigDecimal("1000.00"));
        account.setPlazoMaximoMeses(4);
        account.setDiaCorte(15);
        account.setActive(true);
        return clientAccountRepository.save(account);
    }

    private List<Product> createProducts(Store store){
        return List.of(
                getOrCreateProduct(store, "Piñata unicornio", null, "Piñata grande de unicornio para cumpleaños y celebraciones", "85.00", "95.00"),
                getOrCreateProduct(store, "Pack de 200 globos", null, "Pack surtido de 200 globos para decoración de fiestas", "72.00", "82.00"),
                getOrCreateProduct(store, "Pack de 12 globos temáticos", "Pack de globos", "Doce globos temáticos para complementar la decoración", "48.00", "56.00"),
                getOrCreateProduct(store, "Pack de 12 peluches", "Pack de peluches", "Doce peluches pequeños para regalos, premios o decoración", "118.00", "132.00"),
                getOrCreateProduct(store, "Pack carpa cumpleañera", null, "Set decorativo tipo carpa para mesa principal de cumpleaños", "145.00", "160.00"),
                getOrCreateProduct(store, "Pack premios para piñata", "Pack de premios", "Surtido de premios pequeños para rellenar la piñata", "55.00", "65.00"),
                getOrCreateProduct(store, "Pack utensilios para comer", "Pack de utensilios", "Vasos, platos y utensilios desechables para una celebración", "60.00", "70.00"),
                getOrCreateProduct(store, "Estante para postres", null, "Estante decorativo para organizar dulces y postres de la mesa", "135.00", "150.00")
        );
    }

    private Product getOrCreateProduct(Store store, String name, String previousName, String description, String cashPrice, String creditPrice){
        Product product = productRepository.findByStore_IdAndName(store.getId(), name)
                .orElseGet(() -> previousName == null
                        ? createProduct(store, name)
                        : productRepository.findByStore_IdAndName(store.getId(), previousName)
                        .orElseGet(() -> createProduct(store, name)));

        product.setName(name);
        product.setSupplier("Dayu");
        product.setBrand("Dayu");
        product.setDescription(description);
        product.setUnitOfMeasure("unidad");
        product.setCashPrice(new BigDecimal(cashPrice));
        product.setCreditPrice(new BigDecimal(creditPrice));
        product.setAllowsInstallments(true);
        product.setActive(true);
        return productRepository.save(product);
    }

    private Product createProduct(Store store, String name){
        Product product = new Product();
        product.setStore(store);
        product.setName(name);
        return product;
    }

    private void createFrenchPlan(ClientAccount account, Product product){
        boolean activePlanExists = creditPlanRepository
                .findFirstByPurchase_ClientAccount_Client_IdAndStatus(account.getClient().getId(), CreditPlanStatus.ACTIVE)
                .isPresent();

        if(activePlanExists) return;

        BigDecimal ventaPrecio = new BigDecimal("500.00");

        Purchase purchase = new Purchase();
        purchase.setClientAccount(account);
        purchase.setPurchaseDate(LocalDate.now().minusDays(40));
        purchase.setPaymentMode(PurchasePaymentMode.INSTALLMENTS);
        purchase.setStatus(PurchaseStatus.FINANCED);
        purchase.setTotal(ventaPrecio);

        PurchaseDetail detail = new PurchaseDetail();
        detail.setPurchase(purchase);
        detail.setProduct(product);
        detail.setQuantity(1);
        detail.setUnitPrice(ventaPrecio);
        detail.setSubtotal(ventaPrecio);
        purchase.getDetails().add(detail);

        purchase = purchaseRepository.save(purchase);
        creditPlanService.createPlan(purchase, ventaPrecio, BigDecimal.ZERO, 4, true);
    }
}
