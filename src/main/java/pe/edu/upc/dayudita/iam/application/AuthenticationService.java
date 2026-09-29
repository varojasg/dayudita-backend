package pe.edu.upc.dayudita.iam.application;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;
import pe.edu.upc.dayudita.iam.domain.repository.AdministratorRepository;
import pe.edu.upc.dayudita.iam.interfaces.rest.dto.SetupSystemAdminRequest;
import pe.edu.upc.dayudita.iam.security.ApplicationUserDetails;
import pe.edu.upc.dayudita.iam.security.ApplicationUserDetailsService;

@Service
public class AuthenticationService {

    private final ApplicationUserDetailsService applicationUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AdministratorRepository administratorRepository;
    private final AdministratorService administratorService;

    public AuthenticationService(
            ApplicationUserDetailsService applicationUserDetailsService,
            PasswordEncoder passwordEncoder,
            AdministratorRepository administratorRepository,
            AdministratorService administratorService
    ){
        this.applicationUserDetailsService = applicationUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.administratorRepository = administratorRepository;
        this.administratorService = administratorService;
    }

    public ApplicationUserDetails login(String email, String password){
        UserDetails userDetails;

        try{
            userDetails = applicationUserDetailsService.loadUserByUsername(email);
        }catch(Exception exception){
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }

        if(!userDetails.isEnabled() || !passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }

        return (ApplicationUserDetails) userDetails;
    }

    @Transactional
    public Administrator setupSystemAdmin(SetupSystemAdminRequest request){
        if(administratorRepository.existsByRole(AdministratorRole.SYSTEM_ADMIN)){
            throw new IllegalArgumentException("El administrador del sistema ya fue configurado");
        }

        Administrator administrator = new Administrator();
        administrator.setFirstName(request.firstName());
        administrator.setLastName(request.lastName());
        administrator.setEmail(request.email());
        administrator.setPassword(request.password());
        administrator.setRole(AdministratorRole.SYSTEM_ADMIN);

        return administratorService.createAdministrator(administrator);
    }
}
