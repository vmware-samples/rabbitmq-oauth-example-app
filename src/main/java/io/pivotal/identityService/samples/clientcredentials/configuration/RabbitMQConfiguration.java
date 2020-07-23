package io.pivotal.identityService.samples.clientcredentials.configuration;


import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import com.rabbitmq.client.impl.DefaultCredentialsRefreshService;
import com.rabbitmq.client.impl.OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
    @Value("${spring.security.oauth2.client.registration.sso.authorization-grant-type}")
    private String grantType;
    @Value("${spring.security.oauth2.client.registration.sso.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.sso.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.sso.issuer-uri}")
    private String tokenEndpointUri;
    @Value("${example.rabbitmq.queue.name}")
    private String queueName;
    @Value("${example.rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${example.rabbitmq.routingkey}")
    private String routingKey;

    @Bean
    public ConnectionFactory connectionFactory(com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitConnectionFactory);
        cachingConnectionFactory.setConnectionNameStrategy(factory -> "sso-example");
        return cachingConnectionFactory;
    }

    @Bean
    public com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory(CredentialsProvider credentialsProvider, CredentialsRefreshService credentialsRefreshService, CfEnv cfEnv){
        CfCredentials rabbitmqCredentials = cfEnv.findCredentialsByTag("rabbitmq");
        Object vhost = rabbitmqCredentials.getMap().get("vhost");
        String host = rabbitmqCredentials.getHost();
        int port = Integer.parseInt(rabbitmqCredentials.getPort());

        com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory = new com.rabbitmq.client.ConnectionFactory();
        rabbitConnectionFactory.setCredentialsProvider(credentialsProvider);
        rabbitConnectionFactory.setCredentialsRefreshService(credentialsRefreshService);
        rabbitConnectionFactory.setPort(port);
        rabbitConnectionFactory.setHost(host);
        rabbitConnectionFactory.setVirtualHost((String)vhost);

        return rabbitConnectionFactory;
    }

    @Bean
    public CredentialsRefreshService oauthCredentialsRefreshService() {
        return new DefaultCredentialsRefreshService.DefaultCredentialsRefreshServiceBuilder().build();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setDefaultReceiveQueue(queueName);
        return rabbitTemplate;
    }

    @Bean
    public CredentialsProvider credentialsProvider() {
        CredentialsProvider credentialsProvider =  new OAuth2ClientCredentialsGrantCredentialsProviderBuilder()
            .tokenEndpointUri(tokenEndpointUri)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .grantType(grantType)
            .build();

            return credentialsProvider;
    }

    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
