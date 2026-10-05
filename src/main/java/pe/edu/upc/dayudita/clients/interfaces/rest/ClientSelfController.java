package pe.edu.upc.dayudita.clients.interfaces.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.dayudita.clients.application.ClientService;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.ClientAccountResponse;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;

import java.util.List;

@RestController
@RequestMapping("/api/clients/me/accounts")
@PreAuthorize("hasRole('CLIENT')")
public class ClientSelfController {

    private final ClientService clientService;
    private final CurrentUserService currentUserService;

    public ClientSelfController(
            ClientService clientService,
            CurrentUserService currentUserService
    ){
        this.clientService = clientService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ClientAccountResponse> getMyAccounts(){
        Long clientId = currentUserService.getCurrentClientId();

        return clientService.getAccountsByClient(clientId)
                .stream()
                .map(account -> new ClientAccountResponse(
                        account.getId(),
                        account.getStore().getId(),
                        account.getStore().getName(),
                        account.getTeaPactada(),
                        account.getTeaMoratoriaPactada(),
                        account.getCurrency(),
                        account.getLimiteCredito(),
                        account.getPlazoMaximoMeses(),
                        account.getDiaCorte(),
                        account.getActive()
                ))
                .toList();
    }
}
