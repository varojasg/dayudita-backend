//Contains the logic of the system and uses the AdministratorRepository
package pe.edu.upc.dayudita.iam.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;
import pe.edu.upc.dayudita.iam.domain.repository.AdministratorRepository;

@Service
public class AdministratorService {
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministratorService(AdministratorRepository administratorRepository, PasswordEncoder passwordEncoder){
        this.administratorRepository=administratorRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public Administrator createAdministrator(Administrator administrator) {
        if (administratorRepository.existsByEmail(administrator.getEmail())) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }

        if (administrator.getRole() == AdministratorRole.STORE_ADMIN && administrator.getStore() == null) {
            throw new IllegalArgumentException("El administrador debe estar asociado a una tienda");
        }

        if(administrator.getRole() == AdministratorRole.SYSTEM_ADMIN){
            administrator.setStore(null);
        }

        administrator.setPassword(passwordEncoder.encode(administrator.getPassword()));
        return administratorRepository.save(administrator);
    }
}
