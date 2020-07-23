package io.pivotal.identityService.samples.clientcredentials.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.identityService.samples.clientcredentials.rabbitmq.RabbitMQClient;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

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
            rabbitMQClient.send("Yay a message! " + new Date().getTime());
            model.addAttribute("connection_status", "Connected and sent message:)");

            Message receivedMessage = rabbitMQClient.receive();
            model.addAttribute("message", "Received Message: " + new String(receivedMessage.getBody()));
        } catch(Exception e) {
            e.printStackTrace();
            model.addAttribute("connection_status", "Failed: " + e.getMessage());
        }

        model.addAttribute("rmq", cfEnv.findCredentialsByTag("rabbitmq").getHost());

        return "info";
    }
}
