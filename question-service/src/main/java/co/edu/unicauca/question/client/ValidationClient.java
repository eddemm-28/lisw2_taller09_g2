package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.ValidationRequestDTO;
import co.edu.unicauca.question.dto.ValidationResultDTO;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST sincrono hacia Validation Service (:8082). */
@Component
public class ValidationClient {

    private final RestClient restClient;

    public ValidationClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory,
                            @Value("${services.validation.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public ValidationResultDTO validate(ValidationRequestDTO request) {
        try {
            return restClient.post()
                    .uri("/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ValidationResultDTO.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException(
                    "Validation Service no disponible: la pregunta no fue validada ni registrada");
        }
    }
}
