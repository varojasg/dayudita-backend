//Contains the logic of the system and uses the StoreRepository
package pe.edu.upc.dayudita.stores.application;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.edu.upc.dayudita.iam.application.AdministratorService;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;

import java.util.List;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final AdministratorService administratorService;


    public StoreService(StoreRepository storeRepository, AdministratorService administratorService){
        this.storeRepository = storeRepository;
        this.administratorService = administratorService;
    }

    public List<Store> getAllStores(){
        return storeRepository.findAll();
    }

    @Transactional
    public Store createStore(String name, String address, String phone, String adminFirstName, String adminLastName, String adminEmail, String adminPassword){
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);

        store = storeRepository.save(store);

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
}
