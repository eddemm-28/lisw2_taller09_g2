package co.edu.unicauca.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";
    public static final String DLX_NAME = "saberpro.dlx.exchange";
    public static final String DLQ_NOTIFICATIONS = "notification.dlq";

    public static final String QUEUE_REVIEWER_ASSIGNED = "notification.reviewer.assigned.queue";
    public static final String QUEUE_QUESTION_REVIEWED = "notification.question.reviewed.queue";
    public static final String QUEUE_VALIDATION_FAILED = "notification.validation.failed.queue";
    public static final String QUEUE_QUESTION_SUBMITTED = "notification.question.submitted.queue";

    /** Cola -> routing key del evento que recibe. */
    private static final Map<String, String> SUBSCRIPTIONS = Map.of(
            QUEUE_REVIEWER_ASSIGNED, "reviewer.assigned",
            QUEUE_QUESTION_REVIEWED, "question.reviewed",
            QUEUE_VALIDATION_FAILED, "question.validation.failed",
            QUEUE_QUESTION_SUBMITTED, "question.submitted");

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    /**
     * Colas durables: si este servicio esta detenido, los eventos se acumulan y se procesan al
     * reiniciarlo. Los mensajes que fallan tras los reintentos terminan en notification.dlq.
     */
    @Bean
    public Declarables notificationTopology() {
        Queue deadLetterQueue = QueueBuilder.durable(DLQ_NOTIFICATIONS).build();
        List<org.springframework.amqp.core.Declarable> declarables = new ArrayList<>();
        declarables.add(deadLetterQueue);
        SUBSCRIPTIONS.forEach((queueName, routingKey) -> {
            Queue queue = QueueBuilder.durable(queueName).deadLetterExchange(DLX_NAME).build();
            Binding binding = BindingBuilder.bind(queue).to(exchange()).with(routingKey);
            Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange()).with(routingKey);
            declarables.add(queue);
            declarables.add(binding);
            declarables.add(deadLetterBinding);
        });
        return new Declarables(declarables);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
