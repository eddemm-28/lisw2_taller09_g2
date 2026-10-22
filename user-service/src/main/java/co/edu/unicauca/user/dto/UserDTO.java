package co.edu.unicauca.user.dto;

import co.edu.unicauca.user.model.Role;
import co.edu.unicauca.user.model.User;

import java.util.Set;

/**
 * Datos basicos del usuario expuestos a los demas microservicios (nunca incluye la contraseña).
 */
public record UserDTO(Long id, String username, String fullName, String email, boolean active, Set<Role> roles) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.isActive(), Set.copyOf(user.getRoles()));
    }
}
