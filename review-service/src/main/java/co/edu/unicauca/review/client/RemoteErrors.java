package co.edu.unicauca.review.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientResponseException;

/** Extrae el campo "message" del cuerpo de error devuelto por otro microservicio. */
final class RemoteErrors {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RemoteErrors() {
    }

    static String message(RestClientResponseException ex, String fallback) {
        try {
            JsonNode body = MAPPER.readTree(ex.getResponseBodyAsString());
            return body.path("message").asText(fallback);
        } catch (Exception parseError) {
            return fallback;
        }
    }
}
