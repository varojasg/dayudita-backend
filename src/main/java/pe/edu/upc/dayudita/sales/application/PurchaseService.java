package pe.edu.upc.dayudita.sales.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.CreditPlanResponse;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.products.domain.model.Product;
import pe.edu.upc.dayudita.products.domain.repository.ProductRepository;
import pe.edu.upc.dayudita.sales.domain.model.*;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.CreatePurchaseRequest;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.PurchaseItemRequest;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final CreditPlanService creditPlanService;
    private final CurrentUserService currentUserService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            ProductRepository productRepository,
            ClientAccountRepository clientAccountRepository,
            FinancialConfigurationService financialConfigurationService,
            CreditPlanService creditPlanService,
            CurrentUserService currentUserService
    ){
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.creditPlanService = creditPlanService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Purchase createPurchase(Long storeId, CreatePurchaseRequest request){
        currentUserService.validateStoreAdmin(storeId);

        ClientAccount account = clientAccountRepository
                .findByClient_IdAndStore_Id(request.clientId(), storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        if(!account.getActive()){
            throw new IllegalArgumentException("El cliente se encuentra inactivo en esta tienda");
        }

        Purchase purchase = new Purchase();
        purchase.setClientAccount(account);
        purchase.setPurchaseDate(request.purchaseDate());
        purchase.setPaymentMode(request.paymentMode());

        BigDecimal total = priceItems(storeId, request, purchase);
        purchase.setTotal(total);

        if(request.paymentMode() == PurchasePaymentMode.CASH){
            purchase.setStatus(PurchaseStatus.PAID);
            return purchaseRepository.save(purchase);
        }

        CreditTerms terms = resolveCreditTerms(request);
        FinancialConfiguration policy = validateCreditPurchase(storeId, request.clientId(), account, terms, total);

        purchase.setStatus(PurchaseStatus.FINANCED);
        purchase = purchaseRepository.save(purchase);

        creditPlanService.createPlan(
                purchase,
                total,
                terms.porcentajeCuotaInicial(),
                terms.numeroMeses(),
                policy.getOtorgaGracia()
        );

        return purchase;
    }

    public CreditPlanResponse simulateInstallmentPurchase(Long storeId, CreatePurchaseRequest request){
        currentUserService.validateStoreAdmin(storeId);

        ClientAccount account = clientAccountRepository
                .findByClient_IdAndStore_Id(request.clientId(), storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        if(!account.getActive()){
            throw new IllegalArgumentException("El cliente se encuentra inactivo en esta tienda");
        }

        BigDecimal total = priceItems(storeId, request, null);
        CreditTerms terms = resolveCreditTerms(request);
        FinancialConfiguration policy = validateCreditPurchase(storeId, request.clientId(), account, terms, total);

        return creditPlanService.simulate(
                total,
                terms.porcentajeCuotaInicial(),
                terms.numeroMeses(),
                policy.getOtorgaGracia(),
                account.getTeaPactada(),
                account.getTeaMoratoriaPactada(),
                request.purchaseDate(),
                account.getDiaCorte()
        );
    }

    public List<Purchase> getPurchasesByStore(Long storeId){
        currentUserService.validateStoreAdmin(storeId);
        return purchaseRepository.findByClientAccount_Store_IdOrderByPurchaseDateDesc(storeId);
    }

    public List<Purchase> getPurchasesByClient(Long clientId){
        return purchaseRepository.findByClientAccount_Client_IdOrderByPurchaseDateDesc(clientId);
    }

    private BigDecimal priceItems(Long storeId, CreatePurchaseRequest request, Purchase purchase){
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> productIds = new HashSet<>();

        for(PurchaseItemRequest itemRequest : request.items()){
            if(!productIds.add(itemRequest.productId())){
                throw new IllegalArgumentException("Un producto no puede repetirse dentro de la misma compra");
            }

            Product product = productRepository.findByIdAndStore_Id(itemRequest.productId(), storeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Uno de los productos no pertenece a esta tienda"
                    ));

            if(!product.getActive()){
                throw new IllegalArgumentException("Uno de los productos se encuentra inactivo");
            }

            if(request.paymentMode() == PurchasePaymentMode.INSTALLMENTS && !product.getAllowsInstallments()){
                throw new IllegalArgumentException("Uno de los productos no permite pago en cuotas");
            }

            BigDecimal unitPrice = request.paymentMode() == PurchasePaymentMode.CASH
                    ? product.getCashPrice()
                    : product.getCreditPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            if(purchase != null){
                PurchaseDetail detail = new PurchaseDetail();
                detail.setPurchase(purchase);
                detail.setProduct(product);
                detail.setQuantity(itemRequest.quantity());
                detail.setUnitPrice(unitPrice);
                detail.setSubtotal(subtotal);
                purchase.getDetails().add(detail);
            }

            total = total.add(subtotal);
        }

        return total.setScale(2);
    }

    private CreditTerms resolveCreditTerms(CreatePurchaseRequest request){
        if(request.numeroMeses() == null){
            throw new IllegalArgumentException("Debe indicar el numero de meses");
        }

        BigDecimal porcentajeCuotaInicial = request.porcentajeCuotaInicial() != null
                ? request.porcentajeCuotaInicial()
                : BigDecimal.ZERO;

        return new CreditTerms(porcentajeCuotaInicial, request.numeroMeses());
    }

    private FinancialConfiguration validateCreditPurchase(
            Long storeId,
            Long clientId,
            ClientAccount account,
            CreditTerms terms,
            BigDecimal ventaPrecio
    ){
        FinancialConfiguration policy = financialConfigurationService.getConfiguration(storeId);

        BigDecimal cuotaInicial = ventaPrecio.multiply(terms.porcentajeCuotaInicial()).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal principal = ventaPrecio.subtract(cuotaInicial);

        if(principal.compareTo(policy.getCapitalMinimo()) < 0
                || principal.compareTo(policy.getCapitalMaximo()) > 0){
            throw new IllegalArgumentException(
                    "El monto del credito se encuentra fuera del rango permitido por la tienda"
            );
        }

        if(principal.compareTo(account.getLimiteCredito()) > 0){
            throw new IllegalArgumentException(
                    "El monto del credito supera el limite de credito pactado con el cliente"
            );
        }

        BigDecimal teaPactada = account.getTeaPactada();
        if(teaPactada.compareTo(policy.getTeaMinima()) < 0 || teaPactada.compareTo(policy.getTeaMaxima()) > 0){
            throw new IllegalArgumentException(
                    "La TEA pactada con el cliente se encuentra fuera del rango permitido por la tienda"
            );
        }

        if(terms.numeroMeses() < 1 || terms.numeroMeses() > account.getPlazoMaximoMeses()){
            throw new IllegalArgumentException(
                    "El numero de meses supera el plazo maximo pactado con el cliente"
            );
        }

        if(creditPlanService.hasActivePlanForClient(clientId)){
            throw new IllegalArgumentException(
                    "El cliente ya tiene un credito en cuotas activo"
            );
        }

        return policy;
    }

    private record CreditTerms(
            BigDecimal porcentajeCuotaInicial,
            int numeroMeses
    ) {
    }
}
