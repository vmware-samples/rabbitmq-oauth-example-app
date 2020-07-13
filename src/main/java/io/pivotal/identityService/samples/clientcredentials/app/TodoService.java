package io.pivotal.identityService.samples.clientcredentials.app;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TodoService {
    private WebClient webClient;

    public TodoService(WebClient webClient) {
        this.webClient = webClient;
    }
}
