package it.pagopa.ecommerce.payment.instruments.client;

import it.pagopa.generated.ecommerce.apiconfig.v1.api.PaymentServiceProvidersApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ApiConfigClient {

    @Autowired
    @Qualifier("apiConfigWebClient")
    private PaymentServiceProvidersApi apiConfigClient;

    // Just a placeholder for now
    public Flux<String> getPSPs() {
        return Flux.empty();
    }
}

