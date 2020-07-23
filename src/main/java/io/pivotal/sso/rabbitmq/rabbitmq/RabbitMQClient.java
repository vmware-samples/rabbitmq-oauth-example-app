package io.pivotal.sso.rabbitmq.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQClient {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${example.rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${example.rabbitmq.routingkey}")
    private String routingKey;

    private final RabbitTemplate template;

    public RabbitMQClient(RabbitTemplate template) {
        this.template = template;
    }

    public void send(String message) throws PublishException {
        log.info("Sending: {}", message);

        try {
            template.convertAndSend(exchangeName, routingKey, message);
        } catch (AmqpException e) {
            throw new PublishException("Could not publish message: " + message, e);
        }
    }

    public Message receive() {
        Message message = template.receive(1000);
        return message;
    }

    public static class PublishException extends Exception {
        PublishException(String message, Throwable e) {
            super(message, e);
        }
    }
}
