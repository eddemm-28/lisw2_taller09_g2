package co.edu.unicauca.question.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    /**
     * Tiempos maximos de espera para las llamadas REST sincronas: si un servicio no responde,
     * se falla rapido (503) en lugar de bloquear el hilo indefinidamente.
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(
            @Value("${services.http.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${services.http.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return factory;
    }
}
