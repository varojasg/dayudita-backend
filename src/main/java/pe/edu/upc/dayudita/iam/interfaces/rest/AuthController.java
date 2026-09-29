package pe.edu.upc.dayudita.iam.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.iam.application.AuthenticationService;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.iam.interfaces.rest.dto.AuthResponse;
import pe.edu.upc.dayudita.iam.interfaces.rest.dto.LoginRequest;
import pe.edu.upc.dayudita.iam.interfaces.rest.dto.SetupSystemAdminRequest;
import pe.edu.upc.dayudita.iam.security.ApplicationUserDetails;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CurrentUserService currentUserService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(
            AuthenticationService authenticationService,
            CurrentUserService currentUserService
    ){
        this.authenticationService = authenticationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/setup")
    @ResponseStatus(HttpStatus.CREATED)
    public void setupSystemAdmin(@Valid @RequestBody SetupSystemAdminRequest request){
        authenticationService.setupSystemAdmin(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ){
        ApplicationUserDetails user = authenticationService.login(request.email(), request.password());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return toResponse(user);
    }

    @GetMapping("/me")
    public AuthResponse getCurrentUser(){
        return toResponse(currentUserService.getCurrentUser());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null){
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
    }

    private AuthResponse toResponse(ApplicationUserDetails user){
        return new AuthResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getRole(),
                user.getStoreId(),
                user.getStoreName()
        );
    }
}
