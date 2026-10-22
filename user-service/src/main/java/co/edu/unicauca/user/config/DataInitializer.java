package co.edu.unicauca.user.config;

import co.edu.unicauca.user.model.Role;
import co.edu.unicauca.user.model.User;
import co.edu.unicauca.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Usuarios de prueba del taller (los mismos que usa la coleccion de Postman).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        List<User> users = List.of(
                user("docente1", "docente123", "Carolina Muñoz Paz", true, Role.TEACHER, Role.AUTHOR),
                user("revisor1", "revisor123", "Andrés Felipe Rojas", true, Role.REVIEWER),
                user("revisor2", "revisor123", "Luisa Fernanda Ortiz", true, Role.REVIEWER, Role.TEACHER),
                user("admin", "admin123", "Administrador Saber PRO", true, Role.ADMIN),
                user("estudiante1", "estudiante123", "Mateo Hurtado Díaz", true, Role.STUDENT),
                user("revisor3", "revisor123", "Revisor Inactivo", false, Role.REVIEWER));
        userRepository.saveAll(users);
        log.info("[UserService] {} usuarios de prueba registrados", users.size());
    }

    private User user(String username, String password, String fullName, boolean active, Role... roles) {
        return new User(username, passwordEncoder.encode(password), fullName,
                username + "@unicauca.edu.co", active, Set.of(roles));
    }
}
