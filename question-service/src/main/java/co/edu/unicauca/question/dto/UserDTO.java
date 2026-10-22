package co.edu.unicauca.question.dto;

import java.util.Set;

/** Usuario autenticado, segun User Service. */
public record UserDTO(Long id, String username, String fullName, String email, boolean active, Set<String> roles) {

    public boolean hasAnyRole(Set<String> expected) {
        return roles != null && roles.stream().anyMatch(expected::contains);
    }
}
