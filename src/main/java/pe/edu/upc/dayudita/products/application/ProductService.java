package pe.edu.upc.dayudita.products.application;

import org.springframework.stereotype.Service;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.products.domain.model.Product;
import pe.edu.upc.dayudita.products.domain.repository.ProductRepository;
import pe.edu.upc.dayudita.products.interfaces.rest.dto.CreateProductRequest;
import pe.edu.upc.dayudita.products.interfaces.rest.dto.UpdateProductRequest;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final CurrentUserService currentUserService;

    public ProductService(
            ProductRepository productRepository,
            StoreRepository storeRepository,
            ClientAccountRepository clientAccountRepository,
            CurrentUserService currentUserService
    ){
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.currentUserService = currentUserService;
    }

    public List<Product> getProductsByStore(Long storeId){
        currentUserService.validateStoreReadAccess(storeId);
        return productRepository.findByStore_Id(storeId);
    }


    public List<Product> getAvailableProductsForClient(Long clientId, Long storeId){
        ClientAccount account = clientAccountRepository
                .findByClient_IdAndStore_Id(clientId, storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        if(!account.getActive()){
            throw new IllegalArgumentException("El cliente se encuentra inactivo en esta tienda");
        }

        if(!account.getStore().getActive()){
            throw new IllegalArgumentException("La tienda se encuentra inactiva");
        }

        return productRepository.findByStore_IdAndActiveTrue(storeId);
    }

    @Transactional
    public Product createProduct(Long storeId, CreateProductRequest request){
        currentUserService.validateStoreAdmin(storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe"));

        if(!store.getActive()){
            throw new IllegalArgumentException("La tienda se encuentra inactiva");
        }

        validatePaymentModes(request.allowsSinglePayment(), request.allowsInstallments());

        Product product = new Product();
        product.setStore(store);
        product.setName(request.name());
        product.setSupplier(request.supplier());
        product.setBrand(request.brand());
        product.setDescription(request.description());
        product.setUnitOfMeasure(request.unitOfMeasure());
        product.setImageUrl(request.imageUrl());
        product.setCashPrice(request.cashPrice());
        product.setCreditPrice(request.creditPrice());
        product.setAllowsSinglePayment(request.allowsSinglePayment());
        product.setAllowsInstallments(request.allowsInstallments());

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long storeId, Long productId, UpdateProductRequest request){
        currentUserService.validateStoreAdmin(storeId);

        Product product = productRepository.findByIdAndStore_Id(productId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("El producto no pertenece a esta tienda"));

        validatePaymentModes(request.allowsSinglePayment(), request.allowsInstallments());

        product.setName(request.name());
        product.setSupplier(request.supplier());
        product.setBrand(request.brand());
        product.setDescription(request.description());
        product.setUnitOfMeasure(request.unitOfMeasure());
        product.setImageUrl(request.imageUrl());
        product.setCashPrice(request.cashPrice());
        product.setCreditPrice(request.creditPrice());
        product.setAllowsSinglePayment(request.allowsSinglePayment());
        product.setAllowsInstallments(request.allowsInstallments());

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProductStatus(Long storeId, Long productId, Boolean active){
        currentUserService.validateStoreAdmin(storeId);

        Product product = productRepository.findByIdAndStore_Id(productId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("El producto no pertenece a esta tienda"));

        product.setActive(active);
        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(Long storeId, Long productId){
        updateProductStatus(storeId, productId, false);
    }

    private void validatePaymentModes(Boolean allowsSinglePayment, Boolean allowsInstallments){
        if(!allowsSinglePayment && !allowsInstallments){
            throw new IllegalArgumentException("El producto debe permitir al menos una modalidad de credito");
        }
    }
}
