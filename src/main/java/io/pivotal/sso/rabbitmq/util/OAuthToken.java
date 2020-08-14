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
