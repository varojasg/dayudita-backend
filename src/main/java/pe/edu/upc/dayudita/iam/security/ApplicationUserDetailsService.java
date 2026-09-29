package pe.edu.upc.dayudita.iam.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.repository.ClientRepository;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;
import pe.edu.upc.dayudita.iam.domain.repository.AdministratorRepository;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final AdministratorRepository administratorRepository;
    private final ClientRepository clientRepository;

    public ApplicationUserDetailsService(
            AdministratorRepository administratorRepository,
            ClientRepository clientRepository
    ){
        this.administratorRepository = administratorRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email == null ? "" : email.trim();

        return administratorRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(this::fromAdministrator)
                .orElseGet(() -> clientRepository.findByEmailIgnoreCase(normalizedEmail)
                        .map(this::fromClient)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado")));
    }

    private ApplicationUserDetails fromAdministrator(Administrator administrator){
        Long storeId = administrator.getStore() != null ? administrator.getStore().getId() : null;
        String storeName = administrator.getStore() != null ? administrator.getStore().getName() : null;
        boolean enabled = administrator.getActive();

        if(administrator.getRole() == AdministratorRole.STORE_ADMIN){
            enabled = enabled && administrator.getStore() != null && administrator.getStore().getActive();
        }

        return new ApplicationUserDetails(
                administrator.getId(),
                administrator.getFirstName(),
                administrator.getLastName(),
                administrator.getEmail(),
                administrator.getPassword(),
                administrator.getRole().name(),
                storeId,
                storeName,
                enabled
        );
    }

    private ApplicationUserDetails fromClient(Client client){
        return new ApplicationUserDetails(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPassword(),
                "CLIENT",
                null,
                null,
                client.getActive()
        );
    }
}
