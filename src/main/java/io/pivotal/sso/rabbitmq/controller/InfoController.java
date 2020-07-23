package io.pivotal.sso.rabbitmq.controller;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.sso.rabbitmq.rabbitmq.RabbitMQClient;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;
import java.util.UUID;

@Controller
public class InfoController {
    RabbitMQClient rabbitMQClient;

    @Value("${ssoServiceUrl:placeholder}")
    String ssoServiceUrl;

    private CfEnv cfEnv;

    public InfoController(CfEnv cfEnv, RabbitMQClient rabbitMQClient) {
        this.cfEnv = cfEnv;
        this.rabbitMQClient = rabbitMQClient;
    }

    @GetMapping("/")
    public String authorizationCode(Model model) throws Exception {
        // Check if app has been bound to SSO
        if (ssoServiceUrl.equals("placeholder")) {
            model.addAttribute("header", "Warning: You need to bind to the SSO service.");
            model.addAttribute("warning", "Please bind your app to restore regular functionality");
            return "configure_warning";
        }

        try {
            String message = "Generated message: " + UUID.randomUUID().toString();
            rabbitMQClient.send(message);
            String receivedMessage = rabbitMQClient.receive();

            model.addAttribute("message_sent", message);
            model.addAttribute("message_received", receivedMessage);
            model.addAttribute("messages_match", message.equals(receivedMessage));
            model.addAttribute("connection_status", "Connected and sent message:)");
        } catch(Exception e) {
            e.printStackTrace();
            model.addAttribute("connection_status", "Failed: " + e.getMessage());
        }

        model.addAttribute("rmq", cfEnv.findCredentialsByTag("rabbitmq").getHost());

        return "info";
    }
}
