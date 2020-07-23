package io.pivotal.sso.rabbitmq.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.impl.CredentialsProvider;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuthToken {

    private CredentialsProvider credentialsProvider;
    private ObjectMapper objectMapper;

    public static class Token {
        public final String value;
        public final long secondsRemaining;

        public Token(String value, long secondsRemaining) {
            this.value = value;
            this.secondsRemaining = secondsRemaining;
        }
    }

    public OAuthToken(CredentialsProvider credentialsProvider, ObjectMapper objectMapper) {
        this.credentialsProvider = credentialsProvider;
        this.objectMapper = objectMapper;
    }

    public Token getLatestToken() throws Exception {
        String value = toPrettyJsonString(parseToken(credentialsProvider.getPassword()));
        return new Token(value, credentialsProvider.getTimeBeforeExpiration().getSeconds());
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
