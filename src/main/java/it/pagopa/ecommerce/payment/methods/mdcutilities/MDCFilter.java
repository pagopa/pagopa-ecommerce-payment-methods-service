package it.pagopa.ecommerce.payment.methods.mdcutilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MDCFilter implements WebFilter {
    public static final String CONTEXT_KEY = "contextKey";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String HEADER_TRANSACTION_ID = "x-transaction-id";

    @Override
    public Mono<Void> filter(
                             ServerWebExchange exchange,
                             WebFilterChain chain
    ) {
        final HttpHeaders headers = exchange.getRequest().getHeaders();
        final String transactionId = Optional.ofNullable(headers.get(HEADER_TRANSACTION_ID)).orElse(new ArrayList<>())
                .stream()
                .findFirst().orElse("{transactionId-not-found}");

        return chain.filter(exchange)
                .contextWrite(Context.of(CONTEXT_KEY, UUID.randomUUID().toString()))
                .contextWrite(Context.of(TRANSACTION_ID, transactionId));
    }
}
