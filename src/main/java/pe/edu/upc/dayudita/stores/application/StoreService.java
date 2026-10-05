package pe.edu.upc.dayudita.stores.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.iam.application.AdministratorService;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;
import pe.edu.upc.dayudita.stores.interfaces.rest.dto.UpdateStoreRequest;

import java.util.List;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final AdministratorService administratorService;
    private final FinancialConfigurationService financialConfigurationService;

    public StoreService(
            StoreRepository storeRepository,
            AdministratorService administratorService,
            FinancialConfigurationService financialConfigurationService
    ){
        this.storeRepository = storeRepository;
        this.administratorService = administratorService;
        this.financialConfigurationService = financialConfigurationService;
    }

    public List<Store> getAllStores(){
        return storeRepository.findAll();
    }

    public Store getStore(Long storeId){
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe"));
    }

    @Transactional
    public Store createStore(
            String name,
            String address,
            String phone,
            String adminFirstName,
            String adminLastName,
            String adminEmail,
            String adminPassword
    ){
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);

        store = storeRepository.save(store);
        financialConfigurationService.createDefaultConfiguration(store);

        Administrator administrator = new Administrator();
        administrator.setFirstName(adminFirstName);
        administrator.setLastName(adminLastName);
        administrator.setEmail(adminEmail);
        administrator.setPassword(adminPassword);
        administrator.setRole(AdministratorRole.STORE_ADMIN);
        administrator.setStore(store);

        administratorService.createAdministrator(administrator);

        return store;
    }

    @Transactional
    public Store updateStore(Long storeId, UpdateStoreRequest request){
        Store store = getStore(storeId);
        store.setName(request.name());
        store.setAddress(request.address());
        store.setPhone(request.phone());
        return storeRepository.save(store);
    }

    @Transactional
    public Store updateStoreStatus(Long storeId, Boolean active){
        Store store = getStore(storeId);
        store.setActive(active);
        return storeRepository.save(store);
    }
}
