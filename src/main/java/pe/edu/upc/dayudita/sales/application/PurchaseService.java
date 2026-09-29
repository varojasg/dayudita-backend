package pe.edu.upc.dayudita.sales.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.FinanceBalanceService;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.products.domain.model.Product;
import pe.edu.upc.dayudita.products.domain.repository.ProductRepository;
import pe.edu.upc.dayudita.sales.domain.model.*;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.CreatePurchaseRequest;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.PurchaseItemRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final FinanceBalanceService financeBalanceService;
    private final CreditPlanService creditPlanService;
    private final CurrentUserService currentUserService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            ProductRepository productRepository,
            ClientAccountRepository clientAccountRepository,
            FinancialConfigurationService financialConfigurationService,
            FinanceBalanceService financeBalanceService,
            CreditPlanService creditPlanService,
            CurrentUserService currentUserService
    ){
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.financeBalanceService = financeBalanceService;
        this.creditPlanService = creditPlanService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Purchase createPurchase(Long storeId, CreatePurchaseRequest request){
        currentUserService.validateStoreAdmin(storeId);

        if(request.purchaseDate().isAfter(LocalDate.now())){
            throw new IllegalArgumentException("La fecha de compra no puede ser futura");
        }

        ClientAccount account = clientAccountRepository
                .findByClient_IdAndStore_Id(request.clientId(), storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        if(!account.getActive()){
            throw new IllegalArgumentException("El cliente se encuentra inactivo en esta tienda");
        }

        validateInstallmentCount(request);
        BigDecimal selectedAnnualRate = validateAnnualRate(request);

        Purchase purchase = new Purchase();
        purchase.setClientAccount(account);
        purchase.setPurchaseDate(request.purchaseDate());
        purchase.setPaymentMode(request.paymentMode());
        purchase.setInstallmentCount(request.installmentCount());
        purchase.setAnnualEffectiveRate(request.paymentMode() == PurchasePaymentMode.CASH ? null : selectedAnnualRate);

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

            validateProductPaymentMode(product, request.paymentMode());

            BigDecimal unitPrice = request.paymentMode() == PurchasePaymentMode.CASH
                    ? product.getCashPrice()
                    : product.getCreditPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setProduct(product);
            detail.setQuantity(itemRequest.quantity());
            detail.setUnitPrice(unitPrice);
            detail.setSubtotal(subtotal);

            purchase.getDetails().add(detail);
            total = total.add(subtotal);
        }

        total = total.setScale(2);
        purchase.setTotal(total);

        if(request.paymentMode() == PurchasePaymentMode.CASH){
            purchase.setStatus(PurchaseStatus.PAID);
            return purchaseRepository.save(purchase);
        }

        validateCreditPurchase(storeId, request.clientId(), request, total);

        if(request.paymentMode() == PurchasePaymentMode.SINGLE_PAYMENT){
            purchase.setStatus(PurchaseStatus.OPEN);
            return purchaseRepository.save(purchase);
        }

        purchase.setStatus(PurchaseStatus.FINANCED);
        purchase = purchaseRepository.save(purchase);
        creditPlanService.createPlan(purchase, request.installmentCount(), selectedAnnualRate);

        return purchase;
    }

    public List<Purchase> getPurchasesByStore(Long storeId){
        currentUserService.validateStoreAdmin(storeId);
        return purchaseRepository.findByClientAccount_Store_IdOrderByPurchaseDateDesc(storeId);
    }

    public List<Purchase> getPurchasesByClient(Long clientId){
        return purchaseRepository.findByClientAccount_Client_IdOrderByPurchaseDateDesc(clientId);
    }

    private BigDecimal validateAnnualRate(CreatePurchaseRequest request){
        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();

        if(request.paymentMode() == PurchasePaymentMode.CASH){
            return configuration.getAnnualEffectiveRate();
        }

        BigDecimal rate = request.annualEffectiveRate() == null
                ? configuration.getAnnualEffectiveRate()
                : request.annualEffectiveRate();

        if(rate.compareTo(configuration.getMinAnnualEffectiveRate()) < 0
                || rate.compareTo(configuration.getMaxAnnualEffectiveRate()) > 0){
            throw new IllegalArgumentException("La TEA debe encontrarse dentro del rango configurado");
        }

        return rate;
    }

    private void validateInstallmentCount(CreatePurchaseRequest request){
        if(request.paymentMode() == PurchasePaymentMode.INSTALLMENTS && request.installmentCount() == null){
            throw new IllegalArgumentException("Debe indicar la cantidad de cuotas");
        }

        if(request.paymentMode() != PurchasePaymentMode.INSTALLMENTS && request.installmentCount() != null){
            throw new IllegalArgumentException("La cantidad de cuotas solo corresponde a compras en cuotas");
        }
    }

    private void validateProductPaymentMode(Product product, PurchasePaymentMode paymentMode){
        if(paymentMode == PurchasePaymentMode.SINGLE_PAYMENT && !product.getAllowsSinglePayment()){
            throw new IllegalArgumentException("Uno de los productos no permite pago unico a credito");
        }

        if(paymentMode == PurchasePaymentMode.INSTALLMENTS && !product.getAllowsInstallments()){
            throw new IllegalArgumentException("Uno de los productos no permite pago en cuotas");
        }
    }

    private void validateCreditPurchase(
            Long storeId,
            Long clientId,
            CreatePurchaseRequest request,
            BigDecimal total
    ){
        FinancialConfiguration configuration = financialConfigurationService.getConfiguration();

        if(total.compareTo(configuration.getMinCapital()) < 0
                || total.compareTo(configuration.getMaxCapital()) > 0){
            throw new IllegalArgumentException("El monto de la compra se encuentra fuera del rango permitido");
        }

        if(financeBalanceService.hasOutstandingCreditAtOtherStore(clientId, storeId)){
            throw new IllegalArgumentException("El cliente ya tiene una deuda activa en otra tienda");
        }

        BigDecimal outstandingCapital = financeBalanceService.getOutstandingCapital(clientId);

        if(outstandingCapital.add(total).compareTo(configuration.getCreditLimit()) > 0){
            throw new IllegalArgumentException("La compra supera el limite de credito permitido");
        }

        if(request.paymentMode() == PurchasePaymentMode.INSTALLMENTS){
            if(outstandingCapital.compareTo(BigDecimal.ZERO) > 0){
                throw new IllegalArgumentException(
                        "El cliente debe terminar su deuda actual antes de crear un nuevo plan de cuotas"
                );
            }

            if(request.installmentCount() > configuration.getMaxInstallments()){
                throw new IllegalArgumentException("La cantidad de cuotas supera el maximo permitido");
            }
        }

        if(request.paymentMode() == PurchasePaymentMode.SINGLE_PAYMENT
                && creditPlanService.hasActivePlanForClient(clientId)){
            throw new IllegalArgumentException(
                    "El cliente tiene un plan de cuotas activo y solo puede realizar compras al contado"
            );
        }
    }
}
