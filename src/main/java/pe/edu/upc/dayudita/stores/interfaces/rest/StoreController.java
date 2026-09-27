
//Rest controller that receives HTTP petitions and calls the StoreService
package pe.edu.upc.dayudita.stores.interfaces.rest;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.stores.application.StoreService;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.interfaces.rest.dto.CreateStoreRequest;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService){
        this.storeService = storeService;
    }

    @GetMapping
    public List<Store> getAllStores(){
        return storeService.getAllStores();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Store createStore(@Valid @RequestBody CreateStoreRequest request){
        return storeService.createStore(
                request.name(),
                request.address(),
                request.phone(),
                request.adminFirstName(),
                request.adminLastName(),
                request.adminEmail(),
                request.adminPassword()
        );
    }
}
