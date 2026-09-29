package pe.edu.upc.dayudita.iam.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ApplicationUserDetails implements UserDetails {

    private final Long userId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final String role;
    private final Long storeId;
    private final String storeName;
    private final boolean enabled;

    public ApplicationUserDetails(
            Long userId,
            String firstName,
            String lastName,
            String email,
            String password,
            String role,
            Long storeId,
            String storeName,
            boolean enabled
    ){
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.storeId = storeId;
        this.storeName = storeName;
        this.enabled = enabled;
    }

    public Long getUserId(){
        return userId;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getRole(){
        return role;
    }

    public Long getStoreId(){
        return storeId;
    }

    public String getStoreName(){
        return storeName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return email;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }
}
