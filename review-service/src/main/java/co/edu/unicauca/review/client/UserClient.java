package co.edu.unicauca.review.client;

import co.edu.unicauca.review.dto.UserDTO;
import co.edu.unicauca.review.exception.ResourceNotFoundException;
import co.edu.unicauca.review.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
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

    public UserDTO getUser(Long userId) {
        try {
            return restClient.get().uri("/users/{id}", userId).retrieve().body(UserDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("No existe el usuario con id " + userId);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("User Service no disponible: no fue posible verificar el usuario");
        }
    }
}
