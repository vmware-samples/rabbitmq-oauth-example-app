package io.pivotal.identityService.samples.clientcredentials.configuration;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import com.rabbitmq.client.impl.DefaultCredentialsRefreshService;
import com.rabbitmq.client.impl.OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
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

    @Bean(name="oauthConnectionFactory")
    public CachingConnectionFactory cachingConnectionFactory(ConnectionFactory rabbitConnectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitConnectionFactory);
        cachingConnectionFactory.setConnectionNameStrategy(factory -> "sso-example");
        return cachingConnectionFactory;
    }

    @Bean
    public ConnectionFactory rabbitConnectionFactory(CredentialsProvider credentialsProvider, CredentialsRefreshService credentialsRefreshService, CfEnv cfEnv){
        CfCredentials rabbitmqCredentials = cfEnv.findCredentialsByTag("rabbitmq");
        Object vhost = rabbitmqCredentials.getMap().get("vhost");
        String host = rabbitmqCredentials.getHost();
        int port = Integer.parseInt(rabbitmqCredentials.getPort());

        ConnectionFactory rabbitConnectionFactory = new ConnectionFactory();
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
    public RabbitTemplate rabbitTemplate(@Qualifier("oauthConnectionFactory") CachingConnectionFactory cachingConnectionFactory) {
        return new RabbitTemplate(cachingConnectionFactory);
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
}
