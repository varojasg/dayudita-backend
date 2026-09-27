
//Record that separates what enters through the API
package pe.edu.upc.dayudita.stores.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStoreRequest (
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max=100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max=200, message = "La direccion no puede superar los 200 caracteres")
        String address,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String phone,

        @NotBlank(message = "El nombre del administrador es obligatorio")
        String adminFirstName,

        @NotBlank(message = "El apellido del administrador es obligatorio")
        String adminLastName,

        @NotBlank(message = "El correo del administrador es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String adminEmail,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener almenos 6 caracteres")
        String adminPassword
){
}
