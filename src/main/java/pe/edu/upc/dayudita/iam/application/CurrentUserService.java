package pe.edu.upc.dayudita.iam.application;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pe.edu.upc.dayudita.iam.security.ApplicationUserDetails;
import pe.edu.upc.dayudita.stores.domain.repository.StoreRepository;

@Service
public class CurrentUserService {

    private final StoreRepository storeRepository;

    public CurrentUserService(StoreRepository storeRepository){
        this.storeRepository = storeRepository;
    }

    public ApplicationUserDetails getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !(authentication.getPrincipal() instanceof ApplicationUserDetails user)){
            throw new AccessDeniedException("No existe un usuario autenticado");
        }

        return user;
    }

    public void validateStoreAdmin(Long storeId){
        ApplicationUserDetails user = getCurrentUser();

        if(!"STORE_ADMIN".equals(user.getRole()) || !storeId.equals(user.getStoreId())){
            throw new AccessDeniedException("No tienes permisos para administrar esta tienda");
        }

        boolean storeActive = storeRepository.findById(storeId)
                .map(store -> store.getActive())
                .orElse(false);

        if(!storeActive){
            throw new AccessDeniedException("La tienda se encuentra inactiva");
        }
    }

    public void validateStoreReadAccess(Long storeId){
        ApplicationUserDetails user = getCurrentUser();

        if("SYSTEM_ADMIN".equals(user.getRole())){
            return;
        }

        if(!"STORE_ADMIN".equals(user.getRole()) || !storeId.equals(user.getStoreId())){
            throw new AccessDeniedException("No tienes permisos para consultar esta tienda");
        }
    }

    public Long getCurrentClientId(){
        ApplicationUserDetails user = getCurrentUser();

        if(!"CLIENT".equals(user.getRole())){
            throw new AccessDeniedException("Esta operacion solo esta disponible para clientes");
        }

        return user.getUserId();
    }
}
