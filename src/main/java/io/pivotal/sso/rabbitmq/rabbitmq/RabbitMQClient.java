/**
 * Copyright 2020 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
