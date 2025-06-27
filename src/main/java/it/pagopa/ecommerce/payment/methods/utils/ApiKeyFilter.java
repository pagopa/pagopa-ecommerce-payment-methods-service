package it.pagopa.ecommerce.payment.methods.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ApiKeyFilter implements WebFilter {

    private final String primaryApiKey;
    private final String secondaryApiKey;
    private final List<String> securedPaths;
    private final Set<String> validApiKeys;

    public ApiKeyFilter(
            @Value("${security.apiKey.primary}") String primaryApiKey,
            @Value("${security.apiKey.secondary}") String secondaryApiKey,
            @Value("${security.apiKey.securedPaths}") List<String> securedPaths
    ) {
        this.primaryApiKey = primaryApiKey;
        this.secondaryApiKey = secondaryApiKey;
        this.securedPaths = securedPaths;
        this.validApiKeys = Set.of(primaryApiKey, secondaryApiKey);
    }

    @Override
    public Mono<Void> filter(
                             ServerWebExchange exchange,
                             WebFilterChain chain
    ) {
        String path = exchange.getRequest().getPath().toString();

        if (securedPaths.stream().anyMatch(path::startsWith)) {
            String apiKey = exchange.getRequest().getHeaders().getFirst("x-api-key");
            if (!isValidApiKey(apiKey)) {
                log.error("Unauthorized request for path {} - Missing or invalid API key", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            logWhichApiKey(apiKey, path);
        }

        return chain.filter(exchange);
    }

    private boolean isValidApiKey(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty() && validApiKeys.contains(apiKey);
    }

    private void logWhichApiKey(
                                String apiKey,
                                String path
    ) {
        String apiKeyType;
        if (primaryApiKey.equals(apiKey)) {
            apiKeyType = "primary";
        } else if (secondaryApiKey.equals(apiKey)) {
            apiKeyType = "secondary";
        } else {
            apiKeyType = "unknown";
        }
        log.debug("API key type used for path {}: {}", path, apiKeyType);
    }
}
