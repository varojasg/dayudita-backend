package pe.edu.upc.dayudita.clients.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.clients.domain.repository.ClientAccountRepository;
import pe.edu.upc.dayudita.clients.domain.repository.ClientRepository;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.AssociateClientRequest;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.CreateClientRequest;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.UpdateClientRequest;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.iam.domain.repository.AdministratorRepository;
import pe.edu.upc.dayudita.stores.domain.model.Store;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final StoreRepository storeRepository;
    private final AdministratorRepository administratorRepository;
    private final FinancialConfigurationService financialConfigurationService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final BigDecimal teaMoratoria;

    public ClientService(
            ClientRepository clientRepository,
            ClientAccountRepository clientAccountRepository,
            StoreRepository storeRepository,
            AdministratorRepository administratorRepository,
            FinancialConfigurationService financialConfigurationService,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService,
            @Value("${dayudita.tea-moratoria:0.12}") BigDecimal teaMoratoria
    ){
        this.clientRepository = clientRepository;
        this.clientAccountRepository = clientAccountRepository;
        this.storeRepository = storeRepository;
        this.administratorRepository = administratorRepository;
        this.financialConfigurationService = financialConfigurationService;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.teaMoratoria = teaMoratoria;
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

        if(clientRepository.existsByEmail(request.email())
                || administratorRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }

        if(clientRepository.existsByDocumentNumber(request.documentNumber())){
            throw new IllegalArgumentException("El documento ya se encuentra registrado");
        }

        validateTeaPactada(storeId, request.teaPactada());
        validatePlazoMaximoMeses(storeId, request.plazoMaximoMeses());

        Client client = new Client();
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setDocumentNumber(request.documentNumber());
        client.setEmail(request.email());
        client.setPassword(passwordEncoder.encode(request.password()));
        client.setPhone(request.phone());

        client = clientRepository.save(client);

        ClientAccount account = new ClientAccount();
        account.setClient(client);
        account.setStore(store);
        account.setTeaPactada(request.teaPactada());
        account.setTeaMoratoriaPactada(teaMoratoria);
        account.setCurrency(request.currency());
        account.setLimiteCredito(request.limiteCredito());
        account.setPlazoMaximoMeses(request.plazoMaximoMeses());
        account.setDiaCorte(request.diaCorte());

        return clientAccountRepository.save(account);
    }

    @Transactional
    public ClientAccount associateClient(Long storeId, Long clientId, AssociateClientRequest request){
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

        validateTeaPactada(storeId, request.teaPactada());
        validatePlazoMaximoMeses(storeId, request.plazoMaximoMeses());

        ClientAccount existingAccount = clientAccountRepository
                .findByClient_IdAndStore_Id(clientId, storeId)
                .orElse(null);

        if(existingAccount != null){
            if(existingAccount.getActive()){
                throw new IllegalArgumentException("El cliente ya se encuentra asociado a esta tienda");
            }

            applyAccountTerms(existingAccount, request);
            existingAccount.setActive(true);

            return clientAccountRepository.save(existingAccount);
        }

        ClientAccount account = new ClientAccount();
        account.setClient(client);
        account.setStore(store);
        applyAccountTerms(account, request);

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

        validateTeaPactada(storeId, request.teaPactada());
        validatePlazoMaximoMeses(storeId, request.plazoMaximoMeses());

        Client client = account.getClient();
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setPhone(request.phone());

        account.setTeaPactada(request.teaPactada());
        account.setTeaMoratoriaPactada(teaMoratoria);
        account.setCurrency(request.currency());
        account.setLimiteCredito(request.limiteCredito());
        account.setPlazoMaximoMeses(request.plazoMaximoMeses());
        account.setDiaCorte(request.diaCorte());

        clientRepository.save(client);
        return clientAccountRepository.save(account);
    }

    private void applyAccountTerms(ClientAccount account, AssociateClientRequest request){
        account.setTeaPactada(request.teaPactada());
        account.setTeaMoratoriaPactada(teaMoratoria);
        account.setCurrency(request.currency());
        account.setLimiteCredito(request.limiteCredito());
        account.setPlazoMaximoMeses(request.plazoMaximoMeses());
        account.setDiaCorte(request.diaCorte());
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

    private void validateTeaPactada(Long storeId, BigDecimal teaPactada){
        FinancialConfiguration policy = financialConfigurationService.getConfiguration(storeId);

        if(teaPactada.compareTo(policy.getTeaMinima()) < 0 || teaPactada.compareTo(policy.getTeaMaxima()) > 0){
            throw new IllegalArgumentException(
                    "La TEA pactada debe encontrarse dentro del rango permitido por la tienda"
            );
        }
    }

    private void validatePlazoMaximoMeses(Long storeId, Integer plazoMaximoMeses){
        FinancialConfiguration policy = financialConfigurationService.getConfiguration(storeId);

        if(plazoMaximoMeses > policy.getPlazoMaximoMeses()){
            throw new IllegalArgumentException(
                    "El plazo maximo pactado no puede superar el plazo maximo permitido por la tienda"
            );
        }
    }
}
