package co.edu.unicauca.question.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";
    public static final String DLX_NAME = "saberpro.dlx.exchange";

    /** Comando SubmitQuestionForReview (consumido por Lifecycle Service). */
    public static final String ROUTING_KEY_SUBMIT = "question.submit";
    /** Evento ValidationFailed (consumido por Notification Service). */
    public static final String ROUTING_KEY_VALIDATION_FAILED = "question.validation.failed";

    /** Resultado de las operaciones asincronas publicado por Lifecycle Service. */
    public static final String QUEUE_OPERATION_RESULT = "question.operation.result.queue";
    public static final String DLQ_OPERATION_RESULT = "question.operation.result.dlq";
    public static final String ROUTING_KEY_OPERATION_RESULT = "question.operation.result";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    @Bean
    public Queue operationResultQueue() {
        return QueueBuilder.durable(QUEUE_OPERATION_RESULT).deadLetterExchange(DLX_NAME).build();
    }

    @Bean
    public Queue operationResultDeadLetterQueue() {
        return QueueBuilder.durable(DLQ_OPERATION_RESULT).build();
    }

    @Bean
    public Binding operationResultBinding() {
        return BindingBuilder.bind(operationResultQueue()).to(exchange()).with(ROUTING_KEY_OPERATION_RESULT);
    }

    @Bean
    public Binding operationResultDeadLetterBinding() {
        return BindingBuilder.bind(operationResultDeadLetterQueue()).to(deadLetterExchange())
                .with(ROUTING_KEY_OPERATION_RESULT);
    }

    /** Serializacion JSON automatica de comandos y eventos (fechas en formato ISO-8601). */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
