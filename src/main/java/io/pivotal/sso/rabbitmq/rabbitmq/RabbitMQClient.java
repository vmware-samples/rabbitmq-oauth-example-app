/**
 * RabbitMQ OAuth Example App
 * Copyright 2020 VMware, Inc.
 *
 * This product is licensed to you under the Apache 2.0 license (the "License").
 * You may not use this product except in compliance with the Apache 2.0 License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */
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

    public String receive() {
        Message message = template.receive(1000);
        return new String(message.getBody());
    }

    public static class PublishException extends Exception {
        PublishException(String message, Throwable e) {
            super(message, e);
        }
    }
}
