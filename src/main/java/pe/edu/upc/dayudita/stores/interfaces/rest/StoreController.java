package pe.edu.upc.dayudita.stores.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.stores.application.StoreService;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.interfaces.rest.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService){
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreResponse> getAllStores(){
        return storeService.getAllStores()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{storeId}")
    public StoreResponse getStore(@PathVariable Long storeId){
        return toResponse(storeService.getStore(storeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponse createStore(@Valid @RequestBody CreateStoreRequest request){
        return toResponse(storeService.createStore(
                request.name(),
                request.address(),
                request.phone(),
                request.adminFirstName(),
                request.adminLastName(),
                request.adminEmail(),
                request.adminPassword()
        ));
    }

    @PutMapping("/{storeId}")
    public StoreResponse updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody UpdateStoreRequest request
    ){
        return toResponse(storeService.updateStore(storeId, request));
    }

    @PatchMapping("/{storeId}/status")
    public StoreResponse updateStoreStatus(
            @PathVariable Long storeId,
            @Valid @RequestBody UpdateStoreStatusRequest request
    ){
        return toResponse(storeService.updateStoreStatus(storeId, request.active()));
    }

    private StoreResponse toResponse(Store store){
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getActive()
        );
    }
}
