package co.edu.unicauca.user.dto;

/**
 * Resultado de la autenticacion. El token es un identificador opaco de sesion (no es JWT;
 * los tokens firmados se implementan en el corte 3 del curso).
 */
public record LoginResponseDTO(boolean authenticated, String token, UserDTO user, String message) {

    public static LoginResponseDTO success(String token, UserDTO user) {
        return new LoginResponseDTO(true, token, user, "Autenticación exitosa");
    }

    public static LoginResponseDTO failure(String message) {
        return new LoginResponseDTO(false, null, null, message);
    }
}
