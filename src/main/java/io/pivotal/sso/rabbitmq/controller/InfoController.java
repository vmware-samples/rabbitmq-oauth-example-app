package io.pivotal.sso.rabbitmq.controller;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.sso.rabbitmq.rabbitmq.RabbitMQClient;
import io.pivotal.sso.rabbitmq.util.OAuthToken;
import io.pivotal.sso.rabbitmq.util.OAuthToken.Token;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class InfoController {
    private RabbitMQClient rabbitMQClient;
    private CfEnv cfEnv;
    private OAuthToken oAuthToken;

    public InfoController(CfEnv cfEnv, RabbitMQClient rabbitMQClient, OAuthToken oAuthTokenParser) {
        this.cfEnv = cfEnv;
        this.rabbitMQClient = rabbitMQClient;
        this.oAuthToken = oAuthTokenParser;
    }

    @GetMapping("/")
    public String authorizationCode(Model model) throws Exception {
        try {
            String message = "Generated message: " + UUID.randomUUID().toString();
            rabbitMQClient.send(message);
            String receivedMessage = rabbitMQClient.receive();

            model.addAttribute("message_sent", message);
            model.addAttribute("message_received", receivedMessage);
            model.addAttribute("messages_match", message.equals(receivedMessage));
            model.addAttribute("connection_status", "Connected and sent message:)");

            Token token = oAuthToken.getLatestToken();
            model.addAttribute("token", token.value);
            model.addAttribute("timeBeforeExpiration", token.secondsRemaining);
        } catch(Exception e) {
            e.printStackTrace();
            model.addAttribute("connection_status", "Failed: " + e.getMessage());
        }

        model.addAttribute("rmq", cfEnv.findCredentialsByTag("rabbitmq").getHost());

        return "info";
    }
}
