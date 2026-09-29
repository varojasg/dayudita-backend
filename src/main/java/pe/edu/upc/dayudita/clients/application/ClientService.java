package pe.edu.upc.dayudita.clients.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.clients.domain.repository.ClientRepository;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.CreateClientRequest;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.UpdateClientRequest;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.iam.domain.repository.AdministratorRepository;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final StoreRepository storeRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public ClientService(
            ClientRepository clientRepository,
            ClientAccountRepository clientAccountRepository,
            StoreRepository storeRepository,
            AdministratorRepository administratorRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService
    ){
        this.clientRepository = clientRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.storeRepository = storeRepository;
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    public List<ClientAccount> getClientsByStore(Long storeId){
        currentUserService.validateStoreAdmin(storeId);
        return clientAccountRepository.findByStore_Id(storeId);
    }

    public List<ClientAccount> getAccountsByClient(Long clientId){
        return clientAccountRepository.findByClient_IdAndActiveTrue(clientId);
    }

    public Client getClientByDocument(Long storeId, String documentNumber){
        currentUserService.validateStoreAdmin(storeId);
        return clientRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
    }

    public boolean isClientAssociated(Long storeId, Long clientId){
        currentUserService.validateStoreAdmin(storeId);
        return clientAccountRepository.findByClient_IdAndStore_Id(clientId, storeId).isPresent();
    }

    @Transactional
    public ClientAccount createClient(Long storeId, CreateClientRequest request){
        currentUserService.validateStoreAdmin(storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe"));

        if(!store.getActive()){
            throw new IllegalArgumentException("La tienda se encuentra inactiva");
        }

        if(clientRepository.existsByEmailIgnoreCase(request.email())
                || administratorRepository.existsByEmailIgnoreCase(request.email())){
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }

        if(clientRepository.existsByDocumentNumber(request.documentNumber())){
            throw new IllegalArgumentException("El documento ya se encuentra registrado");
        }

        Client client = new Client();
        client.setFirstName(request.firstName().trim());
        client.setLastName(request.lastName().trim());
        client.setDocumentNumber(request.documentNumber().trim());
        client.setEmail(request.email().trim().toLowerCase());
        client.setPassword(passwordEncoder.encode(request.password()));
        client.setPhone(request.phone() == null ? null : request.phone().trim());
        client.setActive(true);

        client = clientRepository.save(client);

        ClientAccount account = new ClientAccount();
        account.setClient(client);
        account.setStore(store);
        account.setCutoffDay(request.cutoffDay());
        account.setPaymentDay(request.paymentDay());
        account.setActive(true);

        return clientAccountRepository.save(account);
    }

    @Transactional
    public ClientAccount associateClient(
            Long storeId,
            Long clientId,
            Integer cutoffDay,
            Integer paymentDay
    ){
        currentUserService.validateStoreAdmin(storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe"));

        if(!store.getActive()){
            throw new IllegalArgumentException("La tienda se encuentra inactiva");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));

        if(!client.getActive()){
            throw new IllegalArgumentException("El cliente se encuentra inactivo");
        }

        ClientAccount existingAccount = clientAccountRepository
                .findByClient_IdAndStore_Id(clientId, storeId)
                .orElse(null);

        if(existingAccount != null){
            if(existingAccount.getActive()){
                throw new IllegalArgumentException("El cliente ya se encuentra asociado a esta tienda");
            }

            existingAccount.setActive(true);
            existingAccount.setCutoffDay(cutoffDay);
            existingAccount.setPaymentDay(paymentDay);

            return clientAccountRepository.save(existingAccount);
        }

        ClientAccount account = new ClientAccount();
        account.setClient(client);
        account.setStore(store);
        account.setCutoffDay(cutoffDay);
        account.setPaymentDay(paymentDay);

        return clientAccountRepository.save(account);
    }

    @Transactional
    public ClientAccount updateClient(
            Long storeId,
            Long clientId,
            UpdateClientRequest request
    ){
        currentUserService.validateStoreAdmin(storeId);

        ClientAccount account = clientAccountRepository
                .findByClient_IdAndStore_Id(clientId, storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        Client client = account.getClient();
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setPhone(request.phone());

        account.setCutoffDay(request.cutoffDay());
        account.setPaymentDay(request.paymentDay());

        clientRepository.save(client);
        return clientAccountRepository.save(account);
    }

    @Transactional
    public ClientAccount updateClientStatus(Long storeId, Long clientId, Boolean active){
        currentUserService.validateStoreAdmin(storeId);

        ClientAccount account = clientAccountRepository
                .findByClient_IdAndStore_Id(clientId, storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El cliente no se encuentra asociado a esta tienda"
                ));

        account.setActive(active);
        return clientAccountRepository.save(account);
    }

    @Transactional
    public void deactivateClient(Long storeId, Long clientId){
        updateClientStatus(storeId, clientId, false);
    }
}
