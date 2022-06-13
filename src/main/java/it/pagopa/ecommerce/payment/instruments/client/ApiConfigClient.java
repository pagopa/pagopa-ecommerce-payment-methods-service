package it.pagopa.ecommerce.payment.instruments.client;

import it.pagopa.generated.ecommerce.apiconfig.v1.api.PaymentServiceProvidersApi;
import it.pagopa.generated.ecommerce.apiconfig.v1.dto.ServicesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ApiConfigClient {

    @Autowired
    @Qualifier("apiConfigWebClient")
    private PaymentServiceProvidersApi apiConfigClient;

    public Mono<ServicesDto> getPSPs(Integer page, Integer limit, String paymentTypeCode) {
        return apiConfigClient
                .getApiClient()
                .getWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("page", page)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToMono(ServicesDto.class)
                .doOnError(ResponseStatusException.class,
                        error -> log.error("ResponseStatus Error : {}", new Object[] { error }))
                .doOnError(Exception.class,
                        (Exception error) -> log.error("Generic Error : {}", new Object[] { error }));
    }
}

