package co.edu.unicauca.user.service;

import co.edu.unicauca.user.dto.LoginRequestDTO;
import co.edu.unicauca.user.dto.LoginResponseDTO;
import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.exception.AuthenticationFailedException;
import co.edu.unicauca.user.exception.ResourceNotFoundException;
import co.edu.unicauca.user.model.User;
import co.edu.unicauca.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Sesiones activas: token opaco -> id de usuario. */
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<User> user = userRepository.findByUsername(request.username())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()));
        if (user.isEmpty()) {
            log.warn("[UserService] Login rechazado para '{}': credenciales incorrectas", request.username());
            return LoginResponseDTO.failure("Credenciales incorrectas");
        }
        if (!user.get().isActive()) {
            log.warn("[UserService] Login rechazado para '{}': usuario inactivo", request.username());
            return LoginResponseDTO.failure("El usuario se encuentra inactivo");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.get().getId());
        log.info("[UserService] Login exitoso de '{}' con roles {}", request.username(), user.get().getRoles());
        return LoginResponseDTO.success(token, UserDTO.from(user.get()));
    }

    public UserDTO validateToken(String authorizationHeader) {
        Long userId = extractToken(authorizationHeader).map(sessions::get)
                .orElseThrow(() -> new AuthenticationFailedException("Token de acceso inválido o ausente"));
        return findById(userId);
    }

    public void logout(String authorizationHeader) {
        extractToken(authorizationHeader).ifPresent(sessions::remove);
    }

    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + id));
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(UserDTO::from).toList();
    }

    private Optional<String> extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(authorizationHeader.substring(BEARER_PREFIX.length()).trim());
    }
}
