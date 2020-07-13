package io.pivotal.identityService.samples.clientcredentials.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.impl.CredentialsProvider;
import io.pivotal.cfenv.core.CfEnv;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.io.IOException;
import java.util.Map;

@Controller
public class InfoController {
    @Value("${ssoServiceUrl:placeholder}")
    String ssoServiceUrl;

    private ObjectMapper objectMapper;
    private CfEnv cfEnv;

    public InfoController(ObjectMapper objectMapper, CfEnv cfEnv) {
        this.objectMapper = objectMapper;
        this.cfEnv = cfEnv;
    }

    @GetMapping("/info")
    public String authorizationCode(
            Model model,
            @RegisteredOAuth2AuthorizedClient("sso") OAuth2AuthorizedClient authorizedClient) throws Exception {
        // Check if app has been bound to SSO
        if (ssoServiceUrl.equals("placeholder")) {
            model.addAttribute("header", "Warning: You need to bind to the SSO service.");
            model.addAttribute("warning", "Please bind your app to restore regular functionality");
            return "configure_warning";
        }

        // Display token information
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken != null) {
            String accessTokenValue = accessToken.getTokenValue();
            model.addAttribute("access_token", toPrettyJsonString(parseToken(accessTokenValue)));
            model.addAttribute("rmq", cfEnv.findCredentialsByTag("rabbitmq").getHost());


//            ClientRegistration clientRegistration = authorizedClient.getClientRegistration();
//            CredentialsProvider credentialsProvider =
//                    new OAuth2ClientCredentialsGrantCredentialsProviderBuilder()
//                            .tokenEndpointUri("http://localhost:8080/uaa/oauth/token/")
//                            .clientId("rabbit_client").clientSecret("rabbit_secret")
//                            .grantType("password")
//                            .parameter("username", "rabbit_super")
//                            .parameter("password", "rabbit_super")
//                            .build();

            try {
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
                connectionFactory.setPort(Integer.parseInt(cfEnv.findCredentialsByTag("rabbitmq").getPort()));
                connectionFactory.setHost(cfEnv.findCredentialsByTag("rabbitmq").getHost());
                Object vhost = cfEnv.findCredentialsByTag("rabbitmq").getMap().get("vhost");
                connectionFactory.setVirtualHost((String)vhost);
                connectionFactory.setPassword(accessToken.getTokenValue());
                connectionFactory.setUsername("");
                connectionFactory.setConnectionNameStrategy(factory -> "client-credentials-spike");
                System.out.println("**** TOKEN: " + accessToken.getTokenValue());
                RabbitAdmin admin = new RabbitAdmin(connectionFactory);
                DirectExchange ex = new DirectExchange("ex");
                admin.declareExchange(ex);
                Queue queue = new Queue("q");
                admin.declareQueue(queue);
                admin.declareBinding(BindingBuilder.bind(queue).to(ex).with("foo"));
                RabbitTemplate template = admin.getRabbitTemplate();
//                template.setExchange(ex.getName());
//                template.setDefaultReceiveQueue();
                template.convertAndSend(ex.getName(), "foo", "Yay a message!");
                Message receivedMessage = template.receive(1000);
                model.addAttribute("connection_status", "Connected and sent message:)");
                model.addAttribute("message", "Received Message: " + new String(receivedMessage.getBody()));
            } catch(Exception e) {
                model.addAttribute("connection_status", "Failed: " + e.getMessage());
            }
        }

        return "info";
    }

    private Map<String, ?> parseToken(String base64Token) throws IOException {
        String token = base64Token.split("\\.")[1];
        return objectMapper.readValue(Base64.decodeBase64(token), new TypeReference<Map<String, ?>>() {
        });
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
