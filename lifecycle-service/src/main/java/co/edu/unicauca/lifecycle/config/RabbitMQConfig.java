package co.edu.unicauca.lifecycle.config;

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

    /** Cola de comandos SubmitQuestionForReview publicada por Question Service. */
    public static final String QUEUE_SUBMIT_REVIEW = "question.submit.review.queue";
    public static final String DLQ_SUBMIT_REVIEW = "question.submit.review.dlq";
    public static final String ROUTING_KEY_SUBMIT = "question.submit";

    /** Resultado de la operacion (consumido por Question Service). */
    public static final String ROUTING_KEY_OPERATION_RESULT = "question.operation.result";
    /** Evento de dominio QuestionSubmittedForReview (consumido por Notification Service). */
    public static final String ROUTING_KEY_SUBMITTED = "question.submitted";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    @Bean
    public Queue submitQueue() {
        return QueueBuilder.durable(QUEUE_SUBMIT_REVIEW).deadLetterExchange(DLX_NAME).build();
    }

    @Bean
    public Queue submitDeadLetterQueue() {
        return QueueBuilder.durable(DLQ_SUBMIT_REVIEW).build();
    }

    @Bean
    public Binding bindingSubmit() {
        return BindingBuilder.bind(submitQueue()).to(exchange()).with(ROUTING_KEY_SUBMIT);
    }

    @Bean
    public Binding bindingSubmitDeadLetter() {
        return BindingBuilder.bind(submitDeadLetterQueue()).to(deadLetterExchange()).with(ROUTING_KEY_SUBMIT);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
