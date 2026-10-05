package pe.edu.upc.dayudita.clients.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.clients.application.ClientService;
import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/clients")
@PreAuthorize("hasRole('STORE_ADMIN')")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService){
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientResponse> getClientsByStore(@PathVariable Long storeId){

        return clientService.getClientsByStore(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @GetMapping("/lookup/{documentNumber}")
    public ClientLookupResponse lookupClient(
            @PathVariable Long storeId,
            @PathVariable String documentNumber
    ){
        Client client = clientService.getClientByDocument(storeId, documentNumber);

        return new ClientLookupResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getDocumentNumber(),
                client.getEmail(),
                client.getPhone(),
                clientService.isClientAssociated(storeId, client.getId())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse createClient(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateClientRequest request
    ){
        return toResponse(
                clientService.createClient(storeId, request)
        );
    }

    @PostMapping("/{clientId}/associate")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse associateClient(
            @PathVariable Long storeId,
            @PathVariable Long clientId,
            @Valid @RequestBody AssociateClientRequest request
    ){
        return toResponse(
                clientService.associateClient(storeId, clientId, request)
        );
    }

    @PutMapping("/{clientId}")
    public ClientResponse updateClient(
            @PathVariable Long storeId,
            @PathVariable Long clientId,
            @Valid @RequestBody UpdateClientRequest request
    ){
        return toResponse(
                clientService.updateClient(
                        storeId,
                        clientId,
                        request
                )
        );
    }

    @PatchMapping("/{clientId}/status")
    public ClientResponse updateClientStatus(
            @PathVariable Long storeId,
            @PathVariable Long clientId,
            @Valid @RequestBody UpdateClientStatusRequest request
    ){
        return toResponse(clientService.updateClientStatus(storeId, clientId, request.active()));
    }

    @DeleteMapping("/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateClient(
            @PathVariable Long storeId,
            @PathVariable Long clientId
    ){
        clientService.deactivateClient(storeId, clientId);
    }

    private ClientResponse toResponse(ClientAccount account){

        return new ClientResponse(
                account.getClient().getId(),
                account.getId(),
                account.getClient().getFirstName(),
                account.getClient().getLastName(),
                account.getClient().getDocumentNumber(),
                account.getClient().getEmail(),
                account.getClient().getPhone(),
                account.getTeaPactada(),
                account.getTeaMoratoriaPactada(),
                account.getCurrency(),
                account.getLimiteCredito(),
                account.getPlazoMaximoMeses(),
                account.getDiaCorte(),
                account.getActive()
        );
    }
}
