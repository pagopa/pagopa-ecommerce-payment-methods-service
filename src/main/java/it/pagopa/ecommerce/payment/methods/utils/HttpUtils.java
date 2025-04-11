package it.pagopa.ecommerce.payment.methods.utils;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class HttpUtils {

    public static Mono<String> getAuthenticationToken(ServerWebExchange exchange) {
        return Mono.justOrEmpty(
                Optional.ofNullable(
                        exchange.getRequest()
                                .getHeaders()
                                .get("Authorization")
                )
                        .orElse(List.of())
                        .stream()
                        .findFirst()
                        .filter(header -> header.startsWith("Bearer "))
                        .map(header -> header.substring("Bearer ".length()))
        );
    }
}
