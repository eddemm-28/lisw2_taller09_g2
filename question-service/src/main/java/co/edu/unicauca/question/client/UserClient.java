package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.UserDTO;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import co.edu.unicauca.question.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST sincrono hacia User Service (:8081). */
@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory,
                      @Value("${services.user.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    /** Verifica el encabezado Authorization y devuelve el usuario con sus roles. */
    public UserDTO validateToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new UnauthorizedException("Debe enviar el token de acceso en el encabezado Authorization");
        }
        try {
            return restClient.get()
                    .uri("/auth/validate")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(UserDTO.class);
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new UnauthorizedException(RemoteErrors.message(ex, "Token de acceso inválido"));
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("User Service no disponible: no fue posible verificar el usuario");
        }
    }
}
