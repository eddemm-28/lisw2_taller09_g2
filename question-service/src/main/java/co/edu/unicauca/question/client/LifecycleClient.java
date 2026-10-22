package co.edu.unicauca.question.client;

import co.edu.unicauca.question.dto.LifecycleStateDTO;
import co.edu.unicauca.question.dto.RegisterQuestionRequestDTO;
import co.edu.unicauca.question.dto.TransitionRequestDTO;
import co.edu.unicauca.question.exception.ConflictException;
import co.edu.unicauca.question.exception.ResourceNotFoundException;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/** Cliente REST sincrono hacia Lifecycle Service (:8084), autoridad sobre los estados. */
@Component
public class LifecycleClient {

    private final RestClient restClient;

    public LifecycleClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory,
                           @Value("${services.lifecycle.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    /** Registra el estado inicial DRAFT de una pregunta recien creada. */
    public LifecycleStateDTO registerDraft(Long questionId, Long authorId) {
        try {
            return restClient.post()
                    .uri("/lifecycle/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RegisterQuestionRequestDTO(questionId, authorId))
                    .retrieve()
                    .body(LifecycleStateDTO.class);
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    public LifecycleStateDTO getState(Long questionId) {
        try {
            return restClient.get()
                    .uri("/lifecycle/{questionId}", questionId)
                    .retrieve()
                    .body(LifecycleStateDTO.class);
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    /** Version tolerante a fallos para consultas: si Lifecycle no responde, no se rompe la lectura. */
    public Optional<LifecycleStateDTO> findState(Long questionId) {
        try {
            return Optional.ofNullable(getState(questionId));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public LifecycleStateDTO requestTransition(Long questionId, String targetState, Long changedBy, String comment) {
        try {
            return restClient.post()
                    .uri("/lifecycle/{questionId}/transition", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TransitionRequestDTO(targetState, changedBy, comment))
                    .retrieve()
                    .body(LifecycleStateDTO.class);
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    private RuntimeException translate(RestClientException ex) {
        if (ex instanceof HttpClientErrorException.NotFound notFound) {
            return new ResourceNotFoundException(RemoteErrors.message(notFound, "Pregunta no registrada en Lifecycle Service"));
        }
        if (ex instanceof HttpClientErrorException clientError) {
            return new ConflictException(RemoteErrors.message(clientError, "Lifecycle Service rechazó la transición"));
        }
        return new ServiceUnavailableException("Lifecycle Service no disponible: la operación no se completó");
    }
}
