package co.edu.unicauca.review.client;

import co.edu.unicauca.review.dto.QuestionStateDTO;
import co.edu.unicauca.review.dto.TransitionRequestDTO;
import co.edu.unicauca.review.exception.BadRequestException;
import co.edu.unicauca.review.exception.ConflictException;
import co.edu.unicauca.review.exception.ResourceNotFoundException;
import co.edu.unicauca.review.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST sincrono hacia Lifecycle Service (:8084). */
@Component
public class LifecycleClient {

    private final RestClient restClient;

    public LifecycleClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory,
                           @Value("${services.lifecycle.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public QuestionStateDTO getState(Long questionId) {
        try {
            return restClient.get().uri("/lifecycle/{questionId}", questionId).retrieve()
                    .body(QuestionStateDTO.class);
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    /** Solicita a la autoridad de estados una transicion; Review Service nunca cambia el estado por su cuenta. */
    public QuestionStateDTO requestTransition(Long questionId, String targetState, Long changedBy, String comment) {
        try {
            return restClient.post()
                    .uri("/lifecycle/{questionId}/transition", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TransitionRequestDTO(targetState, changedBy, comment))
                    .retrieve()
                    .body(QuestionStateDTO.class);
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    private RuntimeException translate(RestClientException ex) {
        if (ex instanceof HttpClientErrorException.NotFound notFound) {
            return new ResourceNotFoundException(RemoteErrors.message(notFound, "Pregunta no registrada"));
        }
        if (ex instanceof HttpClientErrorException.BadRequest badRequest) {
            return new BadRequestException(RemoteErrors.message(badRequest, "Transición inválida"));
        }
        if (ex instanceof HttpClientErrorException clientError) {
            return new ConflictException(RemoteErrors.message(clientError, "Lifecycle Service rechazó la transición"));
        }
        return new ServiceUnavailableException("Lifecycle Service no disponible: no se pudo confirmar la transición");
    }
}
